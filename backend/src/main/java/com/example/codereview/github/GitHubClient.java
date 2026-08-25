package com.example.codereview.github;

import com.example.codereview.exception.ApiException;
import com.example.codereview.github.dto.GitHubUserDto;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.util.*;

@Component
@RequiredArgsConstructor
public class GitHubClient {

    private static final Logger log = LoggerFactory.getLogger(GitHubClient.class);
    private final WebClient.Builder webClientBuilder;
    private final ObjectMapper objectMapper;

    @Value("${app.github.client-id:}")
    private String clientId;

    @Value("${app.github.client-secret:}")
    private String clientSecret;

    private static final String GITHUB_API_BASE = "https://api.github.com";

    public String exchangeCodeForToken(String code) {
        try {
            Map<String, String> requestBody = Map.of(
                    "client_id", clientId,
                    "client_secret", clientSecret,
                    "code", code
            );

            String response = webClientBuilder.build()
                    .post()
                    .uri("https://github.com/login/oauth/access_token")
                    .header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(requestBody)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            JsonNode node = objectMapper.readTree(response);
            if (node.has("access_token")) {
                return node.get("access_token").asText();
            } else if (node.has("error_description")) {
                throw new ApiException("GitHub OAuth error: " + node.get("error_description").asText(), HttpStatus.BAD_REQUEST);
            }
            throw new ApiException("Failed to obtain GitHub access token", HttpStatus.BAD_REQUEST);
        } catch (WebClientResponseException e) {
            log.error("GitHub OAuth token exchange failed: {}", e.getResponseBodyAsString());
            throw new ApiException("GitHub token exchange failed: " + e.getMessage(), HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            if (e instanceof ApiException) throw (ApiException) e;
            log.error("GitHub token exchange exception", e);
            throw new ApiException("Error communicating with GitHub: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    public GitHubUserDto getUserProfile(String accessToken) {
        try {
            String response = buildClient(accessToken)
                    .get()
                    .uri(GITHUB_API_BASE + "/user")
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            JsonNode node = objectMapper.readTree(response);
            return GitHubUserDto.builder()
                    .id(node.get("id").asLong())
                    .login(node.get("login").asText())
                    .name(node.hasNonNull("name") ? node.get("name").asText() : node.get("login").asText())
                    .avatarUrl(node.hasNonNull("avatar_url") ? node.get("avatar_url").asText() : null)
                    .htmlUrl(node.hasNonNull("html_url") ? node.get("html_url").asText() : null)
                    .email(node.hasNonNull("email") ? node.get("email").asText() : null)
                    .publicRepos(node.has("public_repos") ? node.get("public_repos").asInt() : 0)
                    .totalPrivateRepos(node.has("total_private_repos") ? node.get("total_private_repos").asInt() : 0)
                    .build();
        } catch (WebClientResponseException e) {
            if (e.getStatusCode() == HttpStatus.UNAUTHORIZED) {
                throw new ApiException("Invalid or expired GitHub access token", HttpStatus.UNAUTHORIZED);
            }
            throw new ApiException("Failed to fetch GitHub profile: " + e.getMessage(), HttpStatus.valueOf(e.getStatusCode().value()));
        } catch (Exception e) {
            if (e instanceof ApiException) throw (ApiException) e;
            throw new ApiException("Error fetching GitHub profile: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    public List<JsonNode> getUserRepositories(String accessToken) {
        try {
            List<JsonNode> allRepos = new ArrayList<>();
            int page = 1;
            int perPage = 100;

            while (page <= 3) { // fetch up to 300 repos
                String response = buildClient(accessToken)
                        .get()
                        .uri(GITHUB_API_BASE + "/user/repos?per_page=" + perPage + "&page=" + page + "&sort=updated&affiliation=owner,collaborator")
                        .retrieve()
                        .bodyToMono(String.class)
                        .block();

                JsonNode arrayNode = objectMapper.readTree(response);
                if (arrayNode.isArray() && arrayNode.size() > 0) {
                    for (JsonNode repoNode : arrayNode) {
                        allRepos.add(repoNode);
                    }
                    if (arrayNode.size() < perPage) break;
                    page++;
                } else {
                    break;
                }
            }
            return allRepos;
        } catch (WebClientResponseException e) {
            throw new ApiException("Failed to fetch GitHub repositories: " + e.getMessage(), HttpStatus.valueOf(e.getStatusCode().value()));
        } catch (Exception e) {
            if (e instanceof ApiException) throw (ApiException) e;
            throw new ApiException("Error fetching GitHub repositories: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    public JsonNode getRepositoryMetadata(String owner, String repo, String accessToken) {
        try {
            String response = buildClient(accessToken)
                    .get()
                    .uri(GITHUB_API_BASE + "/repos/" + owner + "/" + repo)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            return objectMapper.readTree(response);
        } catch (WebClientResponseException e) {
            if (e.getStatusCode() == HttpStatus.NOT_FOUND) {
                String hint = (accessToken == null || accessToken.isBlank())
                        ? ". If this is a private repository, please connect your GitHub account or paste a Personal Access Token first."
                        : ". Please verify the owner/repository name and token permissions.";
                throw new ApiException("Repository not found on GitHub: " + owner + "/" + repo + hint, HttpStatus.NOT_FOUND);
            }
            throw new ApiException("Failed to fetch repository metadata: " + e.getMessage(), HttpStatus.valueOf(e.getStatusCode().value()));
        } catch (Exception e) {
            if (e instanceof ApiException) throw (ApiException) e;
            throw new ApiException("Error fetching repository metadata: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    public JsonNode getGitTree(String owner, String repo, String branch, String accessToken) {
        try {
            String response = buildClient(accessToken)
                    .get()
                    .uri(GITHUB_API_BASE + "/repos/" + owner + "/" + repo + "/git/trees/" + branch + "?recursive=1")
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            return objectMapper.readTree(response);
        } catch (WebClientResponseException e) {
            throw new ApiException("Failed to fetch repository file tree: " + e.getMessage(), HttpStatus.valueOf(e.getStatusCode().value()));
        } catch (Exception e) {
            if (e instanceof ApiException) throw (ApiException) e;
            throw new ApiException("Error fetching repository file tree: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    public String getFileContent(String owner, String repo, String path, String branch, String accessToken) {
        try {
            // First try raw content endpoint for efficient retrieval
            String rawUrl = "https://raw.githubusercontent.com/" + owner + "/" + repo + "/" + branch + "/" + path;
            WebClient client = buildClient(accessToken);

            return client.get()
                    .uri(rawUrl)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();
        } catch (Exception e) {
            // Fallback to GitHub contents API
            try {
                String response = buildClient(accessToken)
                        .get()
                        .uri(GITHUB_API_BASE + "/repos/" + owner + "/" + repo + "/contents/" + path + "?ref=" + branch)
                        .retrieve()
                        .bodyToMono(String.class)
                        .block();

                JsonNode node = objectMapper.readTree(response);
                if (node.has("content") && "base64".equalsIgnoreCase(node.path("encoding").asText())) {
                    String base64Content = node.get("content").asText().replaceAll("\\s+", "");
                    return new String(Base64.getDecoder().decode(base64Content));
                }
                return "";
            } catch (Exception ex) {
                log.warn("Could not retrieve file content for {}: {}", path, ex.getMessage());
                return null;
            }
        }
    }

    private WebClient buildClient(String accessToken) {
        WebClient.Builder builder = WebClient.builder()
                .defaultHeader(HttpHeaders.ACCEPT, "application/vnd.github.v3+json")
                .defaultHeader(HttpHeaders.USER_AGENT, "CodeReviewAI-App");

        if (accessToken != null && !accessToken.isBlank()) {
            builder.defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken);
        }
        return builder.build();
    }
}
