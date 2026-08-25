package com.example.codereview.github;

import com.example.codereview.exception.BadRequestException;
import com.example.codereview.exception.ResourceNotFoundException;
import com.example.codereview.github.dto.GitHubConnectTokenRequest;
import com.example.codereview.github.dto.GitHubUserDto;
import com.example.codereview.user.User;
import com.example.codereview.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Service
@RequiredArgsConstructor
public class GitHubOAuthService {

    private static final Logger log = LoggerFactory.getLogger(GitHubOAuthService.class);

    private final GitHubClient gitHubClient;
    private final GitHubAccountRepository gitHubAccountRepository;
    private final UserRepository userRepository;

    @Value("${app.github.client-id:}")
    private String clientId;

    @Value("${app.github.redirect-uri:http://localhost:5173/auth/github/callback}")
    private String redirectUri;

    public String getAuthorizationUrl(String state) {
        if (clientId == null || clientId.isBlank()) {
            throw new BadRequestException("GitHub Client ID is not configured on the server. You can connect using a Personal Access Token instead.");
        }
        String scope = "repo,read:user,user:email";
        return String.format(
                "https://github.com/login/oauth/authorize?client_id=%s&redirect_uri=%s&scope=%s&state=%s",
                clientId,
                URLEncoder.encode(redirectUri, StandardCharsets.UTF_8),
                URLEncoder.encode(scope, StandardCharsets.UTF_8),
                URLEncoder.encode(state != null ? state : "", StandardCharsets.UTF_8)
        );
    }

    @Transactional
    public GitHubAccount handleOAuthCallback(String code, Long userId) {
        String accessToken = gitHubClient.exchangeCodeForToken(code);
        return saveOrUpdateGitHubAccount(userId, accessToken);
    }

    @Transactional
    public GitHubAccount connectWithToken(Long userId, GitHubConnectTokenRequest request) {
        String token = request.getToken().trim();
        return saveOrUpdateGitHubAccount(userId, token);
    }

    private GitHubAccount saveOrUpdateGitHubAccount(Long userId, String accessToken) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        GitHubUserDto ghUser = gitHubClient.getUserProfile(accessToken);

        GitHubAccount account = gitHubAccountRepository.findFirstByUserIdOrderByIdDesc(userId)
                .orElseGet(() -> GitHubAccount.builder().user(user).build());

        account.setGithubUserId(ghUser.getId());
        account.setGithubUsername(ghUser.getLogin());
        account.setAccessToken(accessToken);
        account.setAvatarUrl(ghUser.getAvatarUrl());
        account.setProfileUrl(ghUser.getHtmlUrl());

        if (user.getAvatarUrl() == null || user.getAvatarUrl().contains("dicebear")) {
            user.setAvatarUrl(ghUser.getAvatarUrl());
            userRepository.save(user);
        }

        return gitHubAccountRepository.save(account);
    }

    @Transactional
    public void disconnect(Long userId) {
        gitHubAccountRepository.deleteByUserId(userId);
    }
}
