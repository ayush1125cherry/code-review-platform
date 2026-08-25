package com.example.codereview.github;

import com.example.codereview.auth.UserPrincipal;
import com.example.codereview.github.dto.GitHubConnectTokenRequest;
import com.example.codereview.github.dto.GitHubRepoDto;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/github")
@RequiredArgsConstructor
public class GitHubController {

    private final GitHubOAuthService gitHubOAuthService;
    private final GitHubRepositoryService gitHubRepositoryService;

    @GetMapping("/connect")
    public ResponseEntity<Map<String, String>> getAuthorizationUrl(@RequestParam(required = false) String state) {
        String authUrl = gitHubOAuthService.getAuthorizationUrl(state);
        return ResponseEntity.ok(Map.of("url", authUrl));
    }

    @PostMapping("/connect-token")
    public ResponseEntity<Map<String, Object>> connectWithToken(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody GitHubConnectTokenRequest request) {
        GitHubAccount account = gitHubOAuthService.connectWithToken(principal.getId(), request);
        return ResponseEntity.ok(Map.of(
                "message", "GitHub connected successfully",
                "username", account.getGithubUsername(),
                "avatarUrl", account.getAvatarUrl() != null ? account.getAvatarUrl() : ""
        ));
    }

    @GetMapping("/repositories")
    public ResponseEntity<List<GitHubRepoDto>> getRepositories(@AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(gitHubRepositoryService.getUserRepositories(principal.getId()));
    }

    @DeleteMapping("/disconnect")
    public ResponseEntity<Map<String, String>> disconnect(@AuthenticationPrincipal UserPrincipal principal) {
        gitHubOAuthService.disconnect(principal.getId());
        return ResponseEntity.ok(Map.of("message", "GitHub disconnected successfully"));
    }
}
