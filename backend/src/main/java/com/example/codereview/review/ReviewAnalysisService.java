package com.example.codereview.review;

import com.example.codereview.ai.CodeReviewAiService;
import com.example.codereview.chat.Conversation;
import com.example.codereview.chat.ConversationRepository;
import com.example.codereview.chat.Message;
import com.example.codereview.chat.MessageRepository;
import com.example.codereview.chat.MessageSender;
import com.example.codereview.github.GitHubAccount;
import com.example.codereview.github.GitHubAccountRepository;
import com.example.codereview.github.GitHubClient;
import com.example.codereview.repository.*;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ReviewAnalysisService {

    private static final Logger log = LoggerFactory.getLogger(ReviewAnalysisService.class);

    private final RepositoryEntityRepository repositoryEntityRepository;
    private final RepositoryFileRepository fileRepository;
    private final RepositoryChunkRepository chunkRepository;
    private final RepositoryFileService fileService;
    private final RepositoryIndexingService indexingService;
    private final CodeReviewAiService codeReviewAiService;
    private final ReviewRepository reviewRepository;
    private final ConversationRepository conversationRepository;
    private final MessageRepository messageRepository;
    private final GitHubClient gitHubClient;
    private final GitHubAccountRepository gitHubAccountRepository;

    @Value("${app.limits.max-files-per-repo:250}")
    private int maxFilesPerRepo;

    @Value("${app.limits.max-file-size-bytes:250000}")
    private long maxFileSizeBytes;

    @Async("repoTaskExecutor")
    public void startAsyncRepositoryReview(Long repositoryId, Long userId, String customApiKey) {
        log.info("Starting background import and review analysis for repo id: {}", repositoryId);
        RepositoryEntity repo = repositoryEntityRepository.findById(repositoryId).orElse(null);
        if (repo == null) {
            log.error("Repository not found for id: {}", repositoryId);
            return;
        }

        try {
            // STEP 1: IMPORTING
            updateProgress(repo, RepoStatus.IMPORTING, "Connecting to GitHub & fetching file tree...", 15);

            Optional<GitHubAccount> ghAccount = gitHubAccountRepository.findFirstByUserIdOrderByIdDesc(userId);
            String accessToken = ghAccount.map(GitHubAccount::getAccessToken).orElse(null);

            String[] parts = repo.getFullName().split("/");
            if (parts.length != 2) {
                throw new IllegalArgumentException("Invalid repository full name: " + repo.getFullName());
            }
            String owner = parts[0];
            String repoName = parts[1];

            // Resolve default branch if not set
            if (repo.getDefaultBranch() == null || repo.getDefaultBranch().isBlank()) {
                JsonNode meta = gitHubClient.getRepositoryMetadata(owner, repoName, accessToken);
                repo.setDefaultBranch(meta.path("default_branch").asText("main"));
                if (repo.getLanguage() == null && meta.hasNonNull("language")) {
                    repo.setLanguage(meta.get("language").asText());
                }
                repositoryEntityRepository.save(repo);
            }

            JsonNode treeResponse = gitHubClient.getGitTree(owner, repoName, repo.getDefaultBranch(), accessToken);
            JsonNode treeArray = treeResponse.path("tree");

            if (!treeArray.isArray() || treeArray.isEmpty()) {
                throw new IllegalStateException("No files found in GitHub repository tree");
            }

            // Filter relevant files
            List<JsonNode> eligibleNodes = new ArrayList<>();
            for (JsonNode node : treeArray) {
                String type = node.path("type").asText();
                String path = node.path("path").asText();
                long size = node.path("size").asLong(0);

                if ("blob".equalsIgnoreCase(type) && !fileService.shouldIgnorePath(path)) {
                    if (size <= maxFileSizeBytes) {
                        eligibleNodes.add(node);
                    }
                }
            }

            if (eligibleNodes.size() > maxFilesPerRepo) {
                eligibleNodes = eligibleNodes.subList(0, maxFilesPerRepo);
            }

            updateProgress(repo, RepoStatus.IMPORTING, "Downloading " + eligibleNodes.size() + " source files...", 30);

            chunkRepository.deleteByRepositoryId(repo.getId());
            fileRepository.deleteByRepositoryId(repo.getId());
            List<RepositoryFile> savedFiles = new ArrayList<>();

            int count = 0;
            for (JsonNode node : eligibleNodes) {
                String path = node.path("path").asText();
                String sha = node.path("sha").asText();
                String fileName = path.contains("/") ? path.substring(path.lastIndexOf('/') + 1) : path;
                String ext = fileName.contains(".") ? fileName.substring(fileName.lastIndexOf('.') + 1) : "";
                String language = fileService.detectLanguage(fileName);

                String content = gitHubClient.getFileContent(owner, repoName, path, repo.getDefaultBranch(), accessToken);
                if (content == null) content = "";

                int lineCount = content.isEmpty() ? 0 : content.split("\\r?\\n").length;
                long byteSize = content.getBytes().length;

                RepositoryFile repoFile = RepositoryFile.builder()
                        .repository(repo)
                        .filePath(path)
                        .fileName(fileName)
                        .fileExtension(ext)
                        .language(language)
                        .lineCount(lineCount)
                        .byteSize(byteSize)
                        .content(content)
                        .isBinary(false)
                        .sha(sha)
                        .build();

                savedFiles.add(repoFile);
                count++;
                if (count % 15 == 0 || count == eligibleNodes.size()) {
                    int progress = 30 + (int) ((count / (double) eligibleNodes.size()) * 25);
                    updateProgress(repo, RepoStatus.IMPORTING, "Downloaded " + count + "/" + eligibleNodes.size() + " files...", progress);
                }
            }

            savedFiles = fileRepository.saveAll(savedFiles);
            log.info("Saved {} files for repository {}", savedFiles.size(), repo.getFullName());

            // STEP 2: INDEXING & EMBEDDINGS
            updateProgress(repo, RepoStatus.INDEXING, "Chunking code and generating vector embeddings...", 60);
            indexingService.indexFiles(repo, savedFiles, customApiKey);

            // STEP 3: ANALYZING WITH GEMINI
            updateProgress(repo, RepoStatus.ANALYZING, "AI analyzing codebase architecture, efficiency & quality...", 80);
            Review review = codeReviewAiService.analyzeAndBuildReview(repo, savedFiles, customApiKey);
            review = reviewRepository.save(review);

            // Create initial conversation
            Conversation conversation = Conversation.builder()
                    .repository(repo)
                    .review(review)
                    .user(repo.getUser())
                    .title("Review Chat - " + repo.getName())
                    .build();
            conversation = conversationRepository.save(conversation);

            // Create welcoming AI message
            String welcomeMessage = String.format(
                    "Hello! I have completed the automated code review for **%s**.\n\n" +
                    "**Overall Score:** %d/100\n" +
                    "**Repository Type:** %s\n\n" +
                    "%s\n\n" +
                    "Feel free to ask any questions about this review, specific files, efficiency improvements, or architectural suggestions!",
                    repo.getFullName(),
                    review.getOverallScore(),
                    review.getRepositoryType(),
                    review.getSummary()
            );

            Message aiMsg = Message.builder()
                    .conversation(conversation)
                    .sender(MessageSender.AI)
                    .content(welcomeMessage)
                    .referencesJson("[]")
                    .build();
            messageRepository.save(aiMsg);

            // STEP 4: READY
            updateProgress(repo, RepoStatus.READY, "Review ready! Overall Score: " + review.getOverallScore() + "/100", 100);
            log.info("Successfully completed review analysis for repo: {} (Score: {})", repo.getFullName(), review.getOverallScore());

        } catch (Exception e) {
            log.error("Failed to complete review analysis for repository id: " + repositoryId, e);
            repo.setStatus(RepoStatus.FAILED);
            repo.setStatusStep("Analysis failed");
            repo.setErrorMessage(e.getMessage() != null ? e.getMessage() : "Unknown error during analysis");
            repositoryEntityRepository.save(repo);
        }
    }

    private void updateProgress(RepositoryEntity repo, RepoStatus status, String step, int progress) {
        repo.setStatus(status);
        repo.setStatusStep(step);
        repo.setStatusProgress(progress);
        if (status != RepoStatus.FAILED) {
            repo.setErrorMessage(null);
        }
        repositoryEntityRepository.save(repo);
    }
}
