package com.example.codereview.chat;

import com.example.codereview.ai.GeminiApiClient;
import com.example.codereview.embedding.VectorSearchService;
import com.example.codereview.repository.RepositoryChunk;
import com.example.codereview.repository.RepositoryEntity;
import com.example.codereview.repository.RepositoryFile;
import com.example.codereview.repository.RepositoryFileRepository;
import com.example.codereview.review.Review;
import com.example.codereview.review.dto.CodeReferenceDto;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
public class AiChatService {

    private static final Logger log = LoggerFactory.getLogger(AiChatService.class);

    private final GeminiApiClient geminiApiClient;
    private final VectorSearchService vectorSearchService;
    private final RepositoryFileRepository fileRepository;
    private final ObjectMapper objectMapper;

    // Matches patterns like [file.java:L10-20] or [path/file.java:10-20] or [path/file.java]
    private static final Pattern CODE_REF_PATTERN = Pattern.compile("\\[([a-zA-Z0-9_./\\\\-]+)(?::L?([0-9]+)(?:-([0-9]+))?)?\\]");

    public record ChatAiResult(String responseText, List<CodeReferenceDto> references) {}

    public ChatAiResult askQuestion(Conversation conversation, String userQuestion, List<Message> history, String customApiKey) {
        RepositoryEntity repo = conversation.getRepository();
        Review review = conversation.getReview();

        // Retrieve relevant code chunks via semantic vector search
        List<RepositoryChunk> relevantChunks = vectorSearchService.searchTopK(repo.getId(), userQuestion, 5, customApiKey);

        String contextPrompt = buildChatPrompt(repo, review, relevantChunks, history, userQuestion);

        String systemInstruction = """
You are Antigravity Code Reviewer, an expert AI assistant discussing a repository code review with a developer.
The user is asking questions about the codebase review findings, architectural design, efficiency bottlenecks, security vulnerabilities, or refactoring options.

GUIDELINES:
1. Ground your answers strictly in the repository code snippets and review findings provided.
2. ALWAYS provide concrete, verifiable code explanations with exact file references.
3. When referencing code files, explicitly use square bracket citations in your answer:
   e.g. "[path/to/MyFile.java:L20-45]" or "[src/security/JwtFilter.java:L15-30]" or "[pom.xml]".
4. Separate EVIDENCE (what exists now) from RECOMMENDATIONS (how to fix or refactor).
5. If the user asks for code improvements or refactoring, provide clean, idiomatic code examples.
6. Be conversational, helpful, concise, and structured.
""";

        String aiResponse = geminiApiClient.generateContent(contextPrompt, systemInstruction, false, customApiKey);
        List<CodeReferenceDto> references = extractReferences(aiResponse, relevantChunks, repo.getId());

        return new ChatAiResult(aiResponse, references);
    }

    private String buildChatPrompt(RepositoryEntity repo, Review review, List<RepositoryChunk> chunks, List<Message> history, String userQuestion) {
        StringBuilder sb = new StringBuilder();

        sb.append("REPOSITORY: ").append(repo.getFullName()).append(" (").append(repo.getLanguage()).append(")\n");
        sb.append("OVERALL SCORE: ").append(review.getOverallScore()).append("/100\n");
        sb.append("EFFICIENCY SCORE: ").append(review.getEfficiencyScore()).append("/100\n");
        sb.append("SECURITY SCORE: ").append(review.getSecurityScore()).append("/100\n");
        sb.append("SUMMARY: ").append(review.getSummary()).append("\n\n");

        sb.append("--- RETRIEVED RELEVANT CODE CHUNKS ---\n");
        if (chunks.isEmpty()) {
            sb.append("No specific matching code chunks retrieved.\n");
        } else {
            for (RepositoryChunk chunk : chunks) {
                sb.append("FILE: ").append(chunk.getFilePath())
                        .append(" (Lines ").append(chunk.getStartLine()).append("-").append(chunk.getEndLine()).append(")\n")
                        .append("```").append(chunk.getLanguage()).append("\n")
                        .append(chunk.getContent()).append("\n```\n\n");
            }
        }

        sb.append("--- CONVERSATION HISTORY ---\n");
        int historyStart = Math.max(0, history.size() - 6);
        for (int i = historyStart; i < history.size(); i++) {
            Message msg = history.get(i);
            sb.append(msg.getSender() == MessageSender.USER ? "User: " : "AI: ")
                    .append(msg.getContent()).append("\n\n");
        }

        sb.append("--- CURRENT USER QUESTION ---\n");
        sb.append("User: ").append(userQuestion).append("\n\n");
        sb.append("AI Response:");

        return sb.toString();
    }

    private List<CodeReferenceDto> extractReferences(String text, List<RepositoryChunk> retrievedChunks, Long repoId) {
        Map<String, CodeReferenceDto> refMap = new LinkedHashMap<>();

        // 1. Extract bracketed citations from text
        Matcher matcher = CODE_REF_PATTERN.matcher(text);
        while (matcher.find()) {
            String pathOrFile = matcher.group(1);
            if (pathOrFile.equalsIgnoreCase("file") || pathOrFile.equalsIgnoreCase("code") || pathOrFile.equalsIgnoreCase("repo")) {
                continue;
            }

            Integer startLine = matcher.group(2) != null ? Integer.parseInt(matcher.group(2)) : 1;
            Integer endLine = matcher.group(3) != null ? Integer.parseInt(matcher.group(3)) : startLine;

            String fileName = pathOrFile.contains("/") ? pathOrFile.substring(pathOrFile.lastIndexOf('/') + 1) : pathOrFile;

            // Try to match with actual repo file
            Optional<RepositoryFile> fileOpt = fileRepository.findByRepositoryIdAndFilePath(repoId, pathOrFile);
            String fullPath = fileOpt.map(RepositoryFile::getFilePath).orElse(pathOrFile);

            refMap.put(fullPath + ":" + startLine, CodeReferenceDto.builder()
                    .file(fileName)
                    .path(fullPath)
                    .startLine(startLine)
                    .endLine(endLine)
                    .comment("Referenced in AI response")
                    .build());
        }

        // 2. If text citation count is low, also add top retrieved chunks as reference cards
        if (refMap.size() < 3) {
            for (RepositoryChunk chunk : retrievedChunks) {
                String key = chunk.getFilePath() + ":" + chunk.getStartLine();
                if (!refMap.containsKey(key)) {
                    String fileName = chunk.getFilePath().contains("/")
                            ? chunk.getFilePath().substring(chunk.getFilePath().lastIndexOf('/') + 1)
                            : chunk.getFilePath();

                    refMap.put(key, CodeReferenceDto.builder()
                            .file(fileName)
                            .path(chunk.getFilePath())
                            .startLine(chunk.getStartLine())
                            .endLine(chunk.getEndLine())
                            .comment("Relevant code context")
                            .build());

                    if (refMap.size() >= 4) break;
                }
            }
        }

        return new ArrayList<>(refMap.values());
    }
}
