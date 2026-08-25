package com.example.codereview.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserDto {
    private Long id;
    private String name;
    private String email;
    private String username;
    private String avatarUrl;
    private boolean githubConnected;
    private String githubUsername;
    private boolean hasCustomGeminiKey;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
