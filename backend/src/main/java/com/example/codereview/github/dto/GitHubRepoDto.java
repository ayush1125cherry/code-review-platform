package com.example.codereview.github.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GitHubRepoDto {
    private Long id;
    private String name;
    private String fullName;
    private String description;
    private String defaultBranch;
    private String language;
    private String htmlUrl;
    private boolean isPrivate;
    private int starsCount;
    private int forksCount;
    private long sizeKb;
    private String updatedAt;
    private boolean alreadyImported;
    private Long localRepoId;
    private Integer lastReviewScore;
}
