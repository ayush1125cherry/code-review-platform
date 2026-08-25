package com.example.codereview.github;

import com.example.codereview.exception.BadRequestException;
import com.example.codereview.github.dto.GitHubRepoDto;
import com.example.codereview.repository.RepositoryEntity;
import com.example.codereview.repository.RepositoryEntityRepository;
import com.example.codereview.review.Review;
import com.example.codereview.review.ReviewRepository;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@RequiredArgsConstructor
public class GitHubRepositoryService {

    private static final Logger log = LoggerFactory.getLogger(GitHubRepositoryService.class);

    private final GitHubClient gitHubClient;
    private final GitHubAccountRepository gitHubAccountRepository;
    private final RepositoryEntityRepository repositoryEntityRepository;
    private final ReviewRepository reviewRepository;

    @Transactional(readOnly = true)
    public List<GitHubRepoDto> getUserRepositories(Long userId) {
        GitHubAccount ghAccount = gitHubAccountRepository.findFirstByUserIdOrderByIdDesc(userId)
                .orElseThrow(() -> new BadRequestException("GitHub account is not connected. Please connect your GitHub account first."));

        List<JsonNode> repoNodes = gitHubClient.getUserRepositories(ghAccount.getAccessToken());
        List<RepositoryEntity> localRepos = repositoryEntityRepository.findByUserIdOrderByUpdatedAtDesc(userId);

        Map<Long, RepositoryEntity> localByGithubId = new HashMap<>();
        Map<String, RepositoryEntity> localByFullName = new HashMap<>();
        for (RepositoryEntity r : localRepos) {
            if (r.getGithubRepoId() != null) {
                localByGithubId.put(r.getGithubRepoId(), r);
            }
            localByFullName.put(r.getFullName().toLowerCase(), r);
        }

        List<GitHubRepoDto> dtos = new ArrayList<>();
        for (JsonNode node : repoNodes) {
            long ghId = node.path("id").asLong();
            String fullName = node.path("full_name").asText();
            String name = node.path("name").asText();
            String description = node.path("description").asText(null);
            String defaultBranch = node.path("default_branch").asText("main");
            String language = node.path("language").asText(null);
            String htmlUrl = node.path("html_url").asText();
            boolean isPrivate = node.path("private").asBoolean(false);
            int stars = node.path("stargazers_count").asInt(0);
            int forks = node.path("forks_count").asInt(0);
            long sizeKb = node.path("size").asLong(0);
            String updatedAt = node.path("updated_at").asText();

            RepositoryEntity matched = localByGithubId.get(ghId);
            if (matched == null) {
                matched = localByFullName.get(fullName.toLowerCase());
            }

            Integer lastScore = null;
            Long localRepoId = null;
            if (matched != null) {
                localRepoId = matched.getId();
                Optional<Review> latestReview = reviewRepository.findFirstByRepositoryIdOrderByCreatedAtDesc(matched.getId());
                if (latestReview.isPresent()) {
                    lastScore = latestReview.get().getOverallScore();
                }
            }

            dtos.add(GitHubRepoDto.builder()
                    .id(ghId)
                    .name(name)
                    .fullName(fullName)
                    .description(description)
                    .defaultBranch(defaultBranch)
                    .language(language)
                    .htmlUrl(htmlUrl)
                    .isPrivate(isPrivate)
                    .starsCount(stars)
                    .forksCount(forks)
                    .sizeKb(sizeKb)
                    .updatedAt(updatedAt)
                    .alreadyImported(matched != null)
                    .localRepoId(localRepoId)
                    .lastReviewScore(lastScore)
                    .build());
        }

        return dtos;
    }
}
