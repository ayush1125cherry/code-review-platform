package com.example.codereview.review.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CodeReferenceDto {
    private String file;
    private String path;
    private Integer startLine;
    private Integer endLine;
    private String snippet;
    private String comment;
}
