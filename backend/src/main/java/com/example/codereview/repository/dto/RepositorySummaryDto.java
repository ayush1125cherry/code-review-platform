package com.example.codereview.repository.dto;

import com.example.codereview.repository.RepoStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RepositorySummaryDto {
    private Long id;
    private Long githubRepoId;
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
    private RepoStatus status;
    private String statusStep;
    private int statusProgress;
    private String errorMessage;
    private Long latestReviewId;
    private Integer latestReviewScore;
    private int totalFiles;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
