package com.example.codereview.review;

import com.example.codereview.chat.Conversation;
import com.example.codereview.chat.ConversationRepository;
import com.example.codereview.exception.ResourceNotFoundException;
import com.example.codereview.repository.RepositoryEntity;
import com.example.codereview.review.dto.*;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CodeReviewService {

    private static final Logger log = LoggerFactory.getLogger(CodeReviewService.class);

    private final ReviewRepository reviewRepository;
    private final ReviewFindingRepository findingRepository;
    private final ConversationRepository conversationRepository;
    private final ObjectMapper objectMapper;

    @Transactional(readOnly = true)
    public ReviewResponseDto getReviewDto(Long reviewId, Long userId) {
        Review review = reviewRepository.findByIdAndUserId(reviewId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Review not found with id: " + reviewId));
        return mapToDto(review);
    }

    @Transactional(readOnly = true)
    public List<ReviewResponseDto> getReviewsByRepository(Long repositoryId, Long userId) {
        return reviewRepository.findByRepositoryIdOrderByCreatedAtDesc(repositoryId)
                .stream()
                .filter(r -> r.getUser().getId().equals(userId))
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ReviewResponseDto> getRecentReviews(Long userId) {
        return reviewRepository.findByUserIdOrderByCreatedAtDesc(userId)
                .stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public void deleteReview(Long reviewId, Long userId) {
        Review review = reviewRepository.findByIdAndUserId(reviewId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Review not found with id: " + reviewId));

        // Delete conversation for this review
        List<Conversation> conversations = conversationRepository.findByReviewIdOrderByUpdatedAtDesc(reviewId);
        conversationRepository.deleteAll(conversations);

        // Delete review (cascade deletes findings)
        reviewRepository.delete(review);
        log.info("Successfully deleted review id {} for user {}", reviewId, userId);
    }

    public ReviewResponseDto mapToDto(Review review) {
        RepositoryEntity repo = review.getRepository();
        List<ReviewFinding> findings = findingRepository.findByReviewId(review.getId());

        List<String> techList = new ArrayList<>();
        if (review.getTechnologiesJson() != null) {
            try {
                techList = objectMapper.readValue(review.getTechnologiesJson(), new TypeReference<List<String>>() {});
            } catch (Exception ignored) {}
        }

        List<ReviewFindingDto> allFindingsDtos = findings.stream()
                .map(this::mapFindingToDto)
                .collect(Collectors.toList());

        List<ReviewFindingDto> strengths = allFindingsDtos.stream()
                .filter(f -> f.getCategory() == FindingCategory.STRENGTH)
                .collect(Collectors.toList());

        List<ReviewFindingDto> weaknesses = allFindingsDtos.stream()
                .filter(f -> f.getCategory() == FindingCategory.WEAKNESS)
                .collect(Collectors.toList());

        List<CodeReferenceDto> allReferences = new ArrayList<>();
        for (ReviewFindingDto f : allFindingsDtos) {
            if (f.getReferences() != null) {
                allReferences.addAll(f.getReferences());
            }
        }

        ScoreBreakdownDto scores = ScoreBreakdownDto.builder()
                .overall(review.getOverallScore())
                .architecture(review.getArchitectureScore())
                .codeQuality(review.getCodeQualityScore())
                .efficiency(review.getEfficiencyScore())
                .security(review.getSecurityScore())
                .maintainability(review.getMaintainabilityScore())
                .testing(review.getTestingScore())
                .documentation(review.getDocumentationScore())
                .build();

        // Get initial conversation ID if present
        Long convId = conversationRepository.findFirstByReviewIdOrderByCreatedAtAsc(review.getId())
                .map(Conversation::getId)
                .orElse(null);

        return ReviewResponseDto.builder()
                .id(review.getId())
                .repositoryId(repo.getId())
                .repositoryName(repo.getName())
                .repositoryFullName(repo.getFullName())
                .defaultBranch(repo.getDefaultBranch())
                .language(repo.getLanguage())
                .conversationId(convId)
                .overallScore(review.getOverallScore())
                .scores(scores)
                .repositoryType(review.getRepositoryType())
                .summary(review.getSummary())
                .technologies(techList)
                .architecture(review.getArchitectureSummary())
                .efficiency(buildCategoryDto("Efficiency", review.getEfficiencyScore(), review.getEfficiencySummary(), FindingCategory.EFFICIENCY, allFindingsDtos))
                .security(buildCategoryDto("Security", review.getSecurityScore(), review.getSecuritySummary(), FindingCategory.SECURITY, allFindingsDtos))
                .maintainability(buildCategoryDto("Maintainability", review.getMaintainabilityScore(), review.getMaintainabilitySummary(), FindingCategory.MAINTAINABILITY, allFindingsDtos))
                .testing(buildCategoryDto("Testing", review.getTestingScore(), review.getTestingSummary(), FindingCategory.TESTING, allFindingsDtos))
                .documentation(buildCategoryDto("Documentation", review.getDocumentationScore(), review.getDocumentationSummary(), FindingCategory.DOCUMENTATION, allFindingsDtos))
                .strengths(strengths)
                .weaknesses(weaknesses)
                .allFindings(allFindingsDtos)
                .allReferences(allReferences)
                .createdAt(review.getCreatedAt())
                .build();
    }

    private CategoryReviewDto buildCategoryDto(String name, int score, String summary, FindingCategory category, List<ReviewFindingDto> allFindings) {
        List<ReviewFindingDto> catFindings = allFindings.stream()
                .filter(f -> f.getCategory() == category)
                .collect(Collectors.toList());

        List<CodeReferenceDto> catRefs = new ArrayList<>();
        for (ReviewFindingDto f : catFindings) {
            if (f.getReferences() != null) {
                catRefs.addAll(f.getReferences());
            }
        }

        return CategoryReviewDto.builder()
                .name(name)
                .score(score)
                .summary(summary)
                .findings(catFindings)
                .references(catRefs)
                .build();
    }

    public ReviewFindingDto mapFindingToDto(ReviewFinding finding) {
        List<CodeReferenceDto> refs = new ArrayList<>();
        if (finding.getReferencesJson() != null && !finding.getReferencesJson().isBlank()) {
            try {
                refs = objectMapper.readValue(finding.getReferencesJson(), new TypeReference<List<CodeReferenceDto>>() {});
            } catch (Exception ignored) {}
        }

        return ReviewFindingDto.builder()
                .id(finding.getId())
                .category(finding.getCategory())
                .severity(finding.getSeverity())
                .title(finding.getTitle())
                .description(finding.getDescription())
                .recommendation(finding.getRecommendation())
                .references(refs)
                .build();
    }
}
