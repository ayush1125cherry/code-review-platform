package com.example.codereview.review.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ScoreBreakdownDto {
    private int overall;
    private int architecture;
    private int codeQuality;
    private int efficiency;
    private int security;
    private int maintainability;
    private int testing;
    private int documentation;
}
