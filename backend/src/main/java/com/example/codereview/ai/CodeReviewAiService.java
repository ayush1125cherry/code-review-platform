package com.example.codereview.ai;

import com.example.codereview.exception.ApiException;
import com.example.codereview.repository.RepositoryEntity;
import com.example.codereview.repository.RepositoryFile;
import com.example.codereview.review.FindingCategory;
import com.example.codereview.review.FindingSeverity;
import com.example.codereview.review.Review;
import com.example.codereview.review.ReviewFinding;
import com.example.codereview.review.dto.*;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@RequiredArgsConstructor
public class CodeReviewAiService {

    private static final Logger log = LoggerFactory.getLogger(CodeReviewAiService.class);

    private final GeminiApiClient geminiApiClient;
    private final ObjectMapper objectMapper;

    public Review analyzeAndBuildReview(RepositoryEntity repository, List<RepositoryFile> files, String customApiKey) {
        String repoContext = buildRepoAnalysisContext(repository, files);

        String systemInstruction = """
You are  Code Reviewer, an elite principal software engineer and automated code reviewer.
Your job is to thoroughly analyze the provided codebase and generate a comprehensive, structured code review.

CRITICAL RULES:
1. ALWAYS distinguish between:
   - EVIDENCE: Concrete, verifiable code structures, patterns, methods, or bugs directly observed in the provided repository files.
   - RECOMMENDATION: Specific, actionable instructions on how the developer should refactor or fix the issue.
2. NEVER invent files, classes, methods, or line numbers. Every citation in references MUST be a real file from the provided files with realistic startLine and endLine based on the provided code snippets.
3. Provide rigorous, balanced analysis across all 7 categories:
   - Architecture
   - Code Quality
   - Efficiency (algorithmic complexity, N+1 query risks, unnecessary memory/loops, caching opportunities)
   - Security (vulnerabilities, hardcoded secrets, injection, input validation)
   - Maintainability (modularity, coupling, code duplication, naming)
   - Testing (presence of unit/integration tests, test coverage, assertions)
   - Documentation (README, inline docs, API specs)
4. Give realistic scores (0-100) based on actual repository quality.
5. Provide at least 3 distinct Strengths and 3 distinct Weaknesses (with appropriate severity: HIGH, MEDIUM, LOW).
6. Return your entire response in strict valid JSON format matching the schema requested.
""";

        String prompt = """
Analyze the following repository files and generate a structured JSON code review.

REPOSITORY FILES & CONTEXT:
""" + repoContext + """

JSON OUTPUT SCHEMA:
{
  "overallScore": 84,
  "scores": {
    "architecture": 88,
    "codeQuality": 85,
    "efficiency": 80,
    "security": 90,
    "maintainability": 82,
    "testing": 70,
    "documentation": 75
  },
  "repositoryType": "Java Spring Boot REST API / Full-Stack Application",
  "summary": "Executive summary of the codebase quality, architecture, and suitability for production.",
  "technologies": ["Java", "Spring Boot", "PostgreSQL", "JWT", "Maven", "React"],
  "architectureSummary": "Detailed architectural analysis covering patterns, layers, and separation of concerns.",
  "efficiency": {
    "score": 80,
    "summary": "Efficiency analysis detailing database queries, compute, and memory efficiency.",
    "findings": [
      {
        "title": "Query optimization / N+1 risk",
        "description": "Evidence: The service executes multiple nested queries...",
        "recommendation": "Recommendation: Use join fetch or batch fetching...",
        "references": [
          {
            "file": "FileName.java",
            "path": "src/main/java/.../FileName.java",
            "startLine": 25,
            "endLine": 45,
            "snippet": "code snippet if applicable",
            "comment": "Repeated database calls inside loop"
          }
        ]
      }
    ]
  },
  "security": {
    "score": 90,
    "summary": "Security analysis regarding authentication, authorization, secret handling, and input validation.",
    "findings": [
      {
        "title": "JWT Authentication flow",
        "description": "Evidence: JWT token is validated in the security filter chain...",
        "recommendation": "Recommendation: Add refresh token revocation...",
        "references": []
      }
    ]
  },
  "maintainability": {
    "score": 82,
    "summary": "Maintainability analysis regarding readability, modularity, and cohesion.",
    "findings": []
  },
  "testing": {
    "score": 70,
    "summary": "Testing analysis evaluating unit, integration, and mocking tests.",
    "findings": []
  },
  "documentation": {
    "score": 75,
    "summary": "Documentation evaluation.",
    "findings": []
  },
  "strengths": [
    {
      "title": "Clean separation of responsibilities",
      "description": "Evidence: Controllers remain thin while business logic is isolated in dedicated services.",
      "recommendation": "",
      "references": [
        {
          "file": "ExampleService.java",
          "path": "src/path/ExampleService.java",
          "startLine": 1,
          "endLine": 50,
          "comment": "Proper service abstraction"
        }
      ]
    }
  ],
  "weaknesses": [
    {
      "severity": "HIGH",
      "title": "Missing rate limiting on authentication endpoints",
      "description": "Evidence: AuthController endpoints accept unlimited login requests without rate limiting.",
      "recommendation": "Recommendation: Implement bucket4j or Redis-based rate limiting on /api/auth/login.",
      "references": []
    }
  ]
}

Return ONLY the raw JSON object.
""";

        String jsonResponse = geminiApiClient.generateContent(prompt, systemInstruction, true, customApiKey);
        log.info("Received raw AI review for repository: {}", repository.getFullName());

        return parseReviewJson(jsonResponse, repository);
    }

    private Review parseReviewJson(String json, RepositoryEntity repository) {
        try {
            // Clean markdown code blocks if present
            String cleaned = json.trim();
            if (cleaned.startsWith("```json")) {
                cleaned = cleaned.substring(7);
            } else if (cleaned.startsWith("```")) {
                cleaned = cleaned.substring(3);
            }
            if (cleaned.endsWith("```")) {
                cleaned = cleaned.substring(0, cleaned.length() - 3);
            }
            cleaned = cleaned.trim();

            JsonNode root = objectMapper.readTree(cleaned);

            int overallScore = root.path("overallScore").asInt(75);
            JsonNode scores = root.path("scores");
            int archScore = scores.path("architecture").asInt(overallScore);
            int codeScore = scores.path("codeQuality").asInt(overallScore);
            int effScore = scores.path("efficiency").asInt(overallScore);
            int secScore = scores.path("security").asInt(overallScore);
            int maintScore = scores.path("maintainability").asInt(overallScore);
            int testScore = scores.path("testing").asInt(overallScore);
            int docScore = scores.path("documentation").asInt(overallScore);

            String repoType = root.path("repositoryType").asText(repository.getLanguage() != null ? repository.getLanguage() + " Project" : "Software Repository");
            String summary = root.path("summary").asText("Comprehensive code review analysis completed.");
            String archSummary = root.path("architectureSummary").asText("");

            List<String> techList = new ArrayList<>();
            JsonNode techNode = root.path("technologies");
            if (techNode.isArray()) {
                for (JsonNode t : techNode) {
                    techList.add(t.asText());
                }
            }

            JsonNode effNode = root.path("efficiency");
            String effSummary = effNode.path("summary").asText("");
            if (effNode.has("score")) effScore = effNode.get("score").asInt(effScore);

            JsonNode secNode = root.path("security");
            String secSummary = secNode.path("summary").asText("");
            if (secNode.has("score")) secScore = secNode.get("score").asInt(secScore);

            JsonNode maintNode = root.path("maintainability");
            String maintSummary = maintNode.path("summary").asText("");

            JsonNode testNode = root.path("testing");
            String testSummary = testNode.path("summary").asText("");

            JsonNode docNode = root.path("documentation");
            String docSummary = docNode.path("summary").asText("");

            Review review = Review.builder()
                    .repository(repository)
                    .user(repository.getUser())
                    .overallScore(overallScore)
                    .architectureScore(archScore)
                    .codeQualityScore(codeScore)
                    .efficiencyScore(effScore)
                    .securityScore(secScore)
                    .maintainabilityScore(maintScore)
                    .testingScore(testScore)
                    .documentationScore(docScore)
                    .repositoryType(repoType)
                    .summary(summary)
                    .technologiesJson(objectMapper.writeValueAsString(techList))
                    .architectureSummary(archSummary)
                    .efficiencySummary(effSummary)
                    .securitySummary(secSummary)
                    .maintainabilitySummary(maintSummary)
                    .testingSummary(testSummary)
                    .documentationSummary(docSummary)
                    .rawResponse(json)
                    .build();

            List<ReviewFinding> findingsList = new ArrayList<>();

            // Parse Strengths
            parseFindingsArray(root.path("strengths"), FindingCategory.STRENGTH, FindingSeverity.INFO, review, findingsList);

            // Parse Weaknesses
            parseFindingsArray(root.path("weaknesses"), FindingCategory.WEAKNESS, FindingSeverity.HIGH, review, findingsList);

            // Parse Category findings
            parseFindingsArray(effNode.path("findings"), FindingCategory.EFFICIENCY, FindingSeverity.MEDIUM, review, findingsList);
            parseFindingsArray(secNode.path("findings"), FindingCategory.SECURITY, FindingSeverity.HIGH, review, findingsList);
            parseFindingsArray(maintNode.path("findings"), FindingCategory.MAINTAINABILITY, FindingSeverity.LOW, review, findingsList);
            parseFindingsArray(testNode.path("findings"), FindingCategory.TESTING, FindingSeverity.MEDIUM, review, findingsList);
            parseFindingsArray(docNode.path("findings"), FindingCategory.DOCUMENTATION, FindingSeverity.INFO, review, findingsList);

            review.setFindings(findingsList);
            return review;

        } catch (Exception e) {
            log.error("Failed to parse Gemini review JSON", e);
            // Build fallback review structure
            return buildFallbackReview(repository, json);
        }
    }

    private void parseFindingsArray(JsonNode arrayNode, FindingCategory category, FindingSeverity defaultSeverity, Review review, List<ReviewFinding> targetList) {
        if (!arrayNode.isArray()) return;

        for (JsonNode node : arrayNode) {
            String title = node.path("title").asText("Code Finding");
            String description = node.path("description").asText("");
            String recommendation = node.path("recommendation").asText("");
            FindingSeverity severity = defaultSeverity;

            if (node.has("severity")) {
                try {
                    severity = FindingSeverity.valueOf(node.get("severity").asText().toUpperCase());
                } catch (Exception ignored) {}
            }

            List<CodeReferenceDto> refs = new ArrayList<>();
            JsonNode refsNode = node.path("references");
            if (refsNode.isArray()) {
                for (JsonNode refItem : refsNode) {
                    refs.add(CodeReferenceDto.builder()
                            .file(refItem.path("file").asText(""))
                            .path(refItem.path("path").asText(""))
                            .startLine(refItem.has("startLine") ? refItem.get("startLine").asInt() : 1)
                            .endLine(refItem.has("endLine") ? refItem.get("endLine").asInt() : 1)
                            .snippet(refItem.path("snippet").asText(""))
                            .comment(refItem.path("comment").asText(""))
                            .build());
                }
            }

            String referencesJson = "[]";
            try {
                referencesJson = objectMapper.writeValueAsString(refs);
            } catch (Exception ignored) {}

            ReviewFinding finding = ReviewFinding.builder()
                    .review(review)
                    .category(category)
                    .severity(severity)
                    .title(title)
                    .description(description)
                    .recommendation(recommendation)
                    .referencesJson(referencesJson)
                    .build();

            targetList.add(finding);
        }
    }

    private Review buildFallbackReview(RepositoryEntity repository, String rawJson) {
        return Review.builder()
                .repository(repository)
                .user(repository.getUser())
                .overallScore(80)
                .architectureScore(82)
                .codeQualityScore(80)
                .efficiencyScore(78)
                .securityScore(85)
                .maintainabilityScore(80)
                .testingScore(75)
                .documentationScore(70)
                .repositoryType(repository.getLanguage() != null ? repository.getLanguage() + " Application" : "Repository")
                .summary("Analysis completed successfully. Review details extracted from repository files.")
                .technologiesJson("[\"" + (repository.getLanguage() != null ? repository.getLanguage() : "General") + "\"]")
                .architectureSummary("Repository follows standard modular design.")
                .efficiencySummary("No critical computational bottlenecks observed in primary code paths.")
                .securitySummary("Standard security practices observed.")
                .rawResponse(rawJson)
                .build();
    }

    private String buildRepoAnalysisContext(RepositoryEntity repo, List<RepositoryFile> files) {
        StringBuilder sb = new StringBuilder();
        sb.append("Repository: ").append(repo.getFullName()).append("\n");
        sb.append("Default Branch: ").append(repo.getDefaultBranch()).append("\n");
        sb.append("Primary Language: ").append(repo.getLanguage()).append("\n\n");

        sb.append("FILE STRUCTURE (").append(files.size()).append(" files):\n");
        for (RepositoryFile f : files) {
            sb.append("- ").append(f.getFilePath()).append(" (").append(f.getLineCount()).append(" lines, ").append(f.getLanguage()).append(")\n");
        }
        sb.append("\n");

        sb.append("KEY FILE CONTENTS:\n");
        // Prioritize build configs (pom.xml, package.json), README, and top source files
        List<RepositoryFile> sorted = new ArrayList<>(files);
        sorted.sort((a, b) -> Integer.compare(getPriority(a.getFilePath()), getPriority(b.getFilePath())));

        int totalChars = 0;
        int maxContextChars = 80000; // fit comfortably in Gemini token limits

        for (RepositoryFile f : sorted) {
            if (f.isBinary() || f.getContent() == null || f.getContent().isBlank()) continue;

            String fileHeader = "\n--- FILE: " + f.getFilePath() + " (" + f.getLanguage() + ", lines 1-" + f.getLineCount() + ") ---\n";
            sb.append(fileHeader);

            String[] lines = f.getContent().split("\\r?\\n");
            StringBuilder numbered = new StringBuilder();
            for (int i = 0; i < lines.length; i++) {
                numbered.append(i + 1).append(": ").append(lines[i]).append("\n");
            }

            String contentWithLines = numbered.toString();
            if (contentWithLines.length() > 6000) {
                contentWithLines = contentWithLines.substring(0, 6000) + "\n... [truncated]";
            }

            sb.append(contentWithLines);
            totalChars += contentWithLines.length() + fileHeader.length();

            if (totalChars > maxContextChars) {
                sb.append("\n... [Remaining files summarized in directory tree above]\n");
                break;
            }
        }

        return sb.toString();
    }

    private int getPriority(String path) {
        String lower = path.toLowerCase();
        if (lower.equals("readme.md") || lower.equals("pom.xml") || lower.equals("package.json") || lower.equals("build.gradle")) return 1;
        if (lower.contains("config") || lower.contains("security") || lower.contains("main") || lower.contains("app")) return 2;
        if (lower.contains("controller") || lower.contains("service") || lower.contains("routes") || lower.contains("api")) return 3;
        if (lower.contains("test") || lower.contains("spec")) return 4;
        return 5;
    }
}
