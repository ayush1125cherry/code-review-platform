package com.example.codereview.repository.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ImportRepoRequest {
    private Long githubRepoId;
    @NotBlank(message = "Repository full name is required (e.g. owner/repo)")
    private String fullName;
    private String defaultBranch;
    private String customApiKey;
}
