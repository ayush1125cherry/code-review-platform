package com.example.codereview.review.dto;

import com.example.codereview.review.FindingCategory;
import com.example.codereview.review.FindingSeverity;
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
public class ReviewFindingDto {
    private Long id;
    private FindingCategory category;
    private FindingSeverity severity;
    private String title;
    private String description;
    private String recommendation;
    @Builder.Default
    private List<CodeReferenceDto> references = new ArrayList<>();
}
