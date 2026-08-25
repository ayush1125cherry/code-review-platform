package com.example.codereview.github.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GitHubUserDto {
    private Long id;
    private String login;
    private String name;
    private String avatarUrl;
    private String htmlUrl;
    private String email;
    private int publicRepos;
    private int totalPrivateRepos;
}
