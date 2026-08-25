package com.example.codereview.user;

import com.example.codereview.auth.UserPrincipal;
import com.example.codereview.chat.MessageRepository;
import com.example.codereview.repository.RepositoryEntityRepository;
import com.example.codereview.review.ReviewRepository;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final RepositoryEntityRepository repositoryEntityRepository;
    private final ReviewRepository reviewRepository;
    private final MessageRepository messageRepository;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DashboardStatsDto {
        private long repositoriesReviewed;
        private long totalRepositories;
        private long questionsAsked;
        private int averageScore;
    }

    @GetMapping("/stats")
    public ResponseEntity<DashboardStatsDto> getStats(@AuthenticationPrincipal UserPrincipal principal) {
        Long userId = principal.getId();
        long reviewCount = reviewRepository.countByUserId(userId);
        long repoCount = repositoryEntityRepository.countByUserId(userId);
        long questionCount = messageRepository.countUserQuestionsByUserId(userId);
        Double avgScore = reviewRepository.getAverageScoreByUserId(userId);

        DashboardStatsDto stats = DashboardStatsDto.builder()
                .repositoriesReviewed(reviewCount)
                .totalRepositories(repoCount)
                .questionsAsked(questionCount)
                .averageScore(avgScore != null ? (int) Math.round(avgScore) : 0)
                .build();

        return ResponseEntity.ok(stats);
    }
}
