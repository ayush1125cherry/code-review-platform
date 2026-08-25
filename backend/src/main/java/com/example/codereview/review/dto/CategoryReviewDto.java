package com.example.codereview.review.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CategoryReviewDto {
    private String name;
    private int score;
    private String summary;
    @Builder.Default
    private List<ReviewFindingDto> findings = new ArrayList<>();
    @Builder.Default
    private List<CodeReferenceDto> references = new ArrayList<>();
}
