package com.example.codereview.repository.dto;

import com.example.codereview.repository.RepoStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RepoStatusResponse {
    private Long repositoryId;
    private RepoStatus status;
    private String step;
    private int progress;
    private String errorMessage;
    private Long reviewId;
}
