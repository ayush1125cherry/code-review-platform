package com.example.codereview.repository;

import com.example.codereview.chat.Conversation;
import com.example.codereview.chat.ConversationRepository;
import com.example.codereview.chat.MessageRepository;
import com.example.codereview.exception.BadRequestException;
import com.example.codereview.exception.ResourceNotFoundException;
import com.example.codereview.github.GitHubAccount;
import com.example.codereview.github.GitHubAccountRepository;
import com.example.codereview.github.GitHubClient;
import com.example.codereview.repository.dto.ImportRepoRequest;
import com.example.codereview.repository.dto.RepoStatusResponse;
import com.example.codereview.repository.dto.RepositorySummaryDto;
import com.example.codereview.review.Review;
import com.example.codereview.review.ReviewAnalysisService;
import com.example.codereview.review.ReviewRepository;
import com.example.codereview.user.User;
import com.example.codereview.user.UserRepository;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RepositoryImportService {

    private static final Logger log = LoggerFactory.getLogger(RepositoryImportService.class);

    private final RepositoryEntityRepository repositoryEntityRepository;
    private final RepositoryFileRepository fileRepository;
    private final RepositoryChunkRepository chunkRepository;
    private final ConversationRepository conversationRepository;
    private final MessageRepository messageRepository;
    private final ReviewRepository reviewRepository;
    private final UserRepository userRepository;
    private final GitHubAccountRepository gitHubAccountRepository;
    private final GitHubClient gitHubClient;
    private final ReviewAnalysisService reviewAnalysisService;

    @Transactional
    public RepositorySummaryDto importAndReview(Long userId, ImportRepoRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userId));

        Optional<GitHubAccount> ghAccount = gitHubAccountRepository.findFirstByUserIdOrderByIdDesc(userId);
        String accessToken = ghAccount.map(GitHubAccount::getAccessToken).orElse(null);

        String fullName = request.getFullName().trim()
                .replaceAll("^(https?://)?(www\\.)?github\\.com/", "")
                .replaceAll("^git@github\\.com:", "")
                .replaceAll("\\.git$", "")
                .replaceAll("^/+", "")
                .replaceAll("/+$", "");

        String[] parts = fullName.split("/");
        if (parts.length != 2 || parts[0].isBlank() || parts[1].isBlank()) {
            throw new BadRequestException("Invalid repository format. Expected format: 'owner/repository' or GitHub URL (e.g. ayush1125cherry/Social-Media-Platform)");
        }

        // Fetch repository metadata from GitHub
        JsonNode meta = gitHubClient.getRepositoryMetadata(parts[0], parts[1], accessToken);

        Long ghId = meta.path("id").asLong();
        String repoName = meta.path("name").asText(parts[1]);
        String description = meta.path("description").asText(null);
        String defaultBranch = request.getDefaultBranch() != null && !request.getDefaultBranch().isBlank()
                ? request.getDefaultBranch()
                : meta.path("default_branch").asText("main");
        String language = meta.path("language").asText(null);
        String htmlUrl = meta.path("html_url").asText("https://github.com/" + fullName);
        boolean isPrivate = meta.path("private").asBoolean(false);
        int stars = meta.path("stargazers_count").asInt(0);
        int forks = meta.path("forks_count").asInt(0);
        long sizeKb = meta.path("size").asLong(0);

        // Find existing or create new repository entity
        RepositoryEntity repo = repositoryEntityRepository.findFirstByUserIdAndFullNameOrderByIdDesc(userId, fullName)
                .orElseGet(() -> RepositoryEntity.builder()
                        .user(user)
                        .fullName(fullName)
                        .name(repoName)
                        .build());

        repo.setGithubRepoId(ghId);
        repo.setName(repoName);
        repo.setDescription(description);
        repo.setDefaultBranch(defaultBranch);
        repo.setLanguage(language);
        repo.setHtmlUrl(htmlUrl);
        repo.setPrivate(isPrivate);
        repo.setStarsCount(stars);
        repo.setForksCount(forks);
        repo.setSizeKb(sizeKb);
        repo.setStatus(RepoStatus.PENDING);
        repo.setStatusStep("Queued for analysis...");
        repo.setStatusProgress(5);
        repo.setErrorMessage(null);

        RepositoryEntity savedRepo = repositoryEntityRepository.save(repo);

        // Trigger asynchronous import and review
        String customApiKey = request.getCustomApiKey() != null && !request.getCustomApiKey().isBlank()
                ? request.getCustomApiKey()
                : user.getGeminiApiKey();

        reviewAnalysisService.startAsyncRepositoryReview(savedRepo.getId(), userId, customApiKey);

        return mapToSummaryDto(savedRepo);
    }

    @Transactional(readOnly = true)
    public List<RepositorySummaryDto> getUserRepositories(Long userId) {
        return repositoryEntityRepository.findByUserIdOrderByUpdatedAtDesc(userId)
                .stream()
                .map(this::mapToSummaryDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public RepositorySummaryDto getRepository(Long repoId, Long userId) {
        RepositoryEntity repo = repositoryEntityRepository.findByIdAndUserId(repoId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Repository not found with id: " + repoId));
        return mapToSummaryDto(repo);
    }

    @Transactional(readOnly = true)
    public RepoStatusResponse getStatus(Long repoId, Long userId) {
        RepositoryEntity repo = repositoryEntityRepository.findByIdAndUserId(repoId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Repository not found: " + repoId));

        Long latestReviewId = null;
        if (repo.getStatus() == RepoStatus.READY) {
            Optional<Review> review = reviewRepository.findFirstByRepositoryIdOrderByCreatedAtDesc(repo.getId());
            if (review.isPresent()) {
                latestReviewId = review.get().getId();
            }
        }

        return RepoStatusResponse.builder()
                .repositoryId(repo.getId())
                .status(repo.getStatus())
                .step(repo.getStatusStep())
                .progress(repo.getStatusProgress())
                .errorMessage(repo.getErrorMessage())
                .reviewId(latestReviewId)
                .build();
    }

    public RepositorySummaryDto mapToSummaryDto(RepositoryEntity repo) {
        Optional<Review> latestReview = reviewRepository.findFirstByRepositoryIdOrderByCreatedAtDesc(repo.getId());
        long totalFiles = fileRepository.countByRepositoryId(repo.getId());

        return RepositorySummaryDto.builder()
                .id(repo.getId())
                .githubRepoId(repo.getGithubRepoId())
                .name(repo.getName())
                .fullName(repo.getFullName())
                .description(repo.getDescription())
                .defaultBranch(repo.getDefaultBranch())
                .language(repo.getLanguage())
                .htmlUrl(repo.getHtmlUrl())
                .isPrivate(repo.isPrivate())
                .starsCount(repo.getStarsCount())
                .forksCount(repo.getForksCount())
                .sizeKb(repo.getSizeKb())
                .status(repo.getStatus())
                .statusStep(repo.getStatusStep())
                .statusProgress(repo.getStatusProgress())
                .errorMessage(repo.getErrorMessage())
                .latestReviewId(latestReview.map(Review::getId).orElse(null))
                .latestReviewScore(latestReview.map(Review::getOverallScore).orElse(null))
                .totalFiles((int) totalFiles)
                .createdAt(repo.getCreatedAt())
                .updatedAt(repo.getUpdatedAt())
                .build();
    }

    @Transactional
    public void deleteRepository(Long repoId, Long userId) {
        RepositoryEntity repo = repositoryEntityRepository.findByIdAndUserId(repoId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Repository not found with id: " + repoId));

        // 1. Delete conversations and messages
        List<Conversation> conversations = conversationRepository.findByRepositoryIdOrderByUpdatedAtDesc(repoId);
        conversationRepository.deleteAll(conversations);

        // 2. Delete reviews and findings
        List<Review> reviews = reviewRepository.findByRepositoryIdOrderByCreatedAtDesc(repoId);
        reviewRepository.deleteAll(reviews);

        // 3. Delete chunks and files
        chunkRepository.deleteByRepositoryId(repoId);
        fileRepository.deleteByRepositoryId(repoId);

        // 4. Delete repository entity
        repositoryEntityRepository.delete(repo);
        log.info("Successfully deleted repository id {} for user {}", repoId, userId);
    }
}
