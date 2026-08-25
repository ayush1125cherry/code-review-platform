package com.example.codereview.github.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GitHubConnectTokenRequest {
    @NotBlank(message = "GitHub Personal Access Token is required")
    private String token;
}
