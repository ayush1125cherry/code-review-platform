package com.example.codereview.review.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReviewResponseDto {
    private Long id;
    private Long repositoryId;
    private String repositoryName;
    private String repositoryFullName;
    private String defaultBranch;
    private String language;
    private Long conversationId;

    private int overallScore;
    private ScoreBreakdownDto scores;

    private String repositoryType;
    private String summary;
    @Builder.Default
    private List<String> technologies = new ArrayList<>();
    private String architecture;

    private CategoryReviewDto efficiency;
    private CategoryReviewDto security;
    private CategoryReviewDto maintainability;
    private CategoryReviewDto testing;
    private CategoryReviewDto documentation;

    @Builder.Default
    private List<ReviewFindingDto> strengths = new ArrayList<>();
    @Builder.Default
    private List<ReviewFindingDto> weaknesses = new ArrayList<>();
    @Builder.Default
    private List<ReviewFindingDto> allFindings = new ArrayList<>();
    @Builder.Default
    private List<CodeReferenceDto> allReferences = new ArrayList<>();

    private LocalDateTime createdAt;
}
