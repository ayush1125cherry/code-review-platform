package com.example.codereview.review;

import com.example.codereview.auth.UserPrincipal;
import com.example.codereview.review.dto.ReviewResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/reviews")
@RequiredArgsConstructor
public class ReviewController {

    private final CodeReviewService codeReviewService;

    @GetMapping("/{id}")
    public ResponseEntity<ReviewResponseDto> getReview(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(codeReviewService.getReviewDto(id, principal.getId()));
    }

    @GetMapping("/repository/{repoId}")
    public ResponseEntity<List<ReviewResponseDto>> getReviewsByRepository(
            @PathVariable Long repoId,
            @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(codeReviewService.getReviewsByRepository(repoId, principal.getId()));
    }

    @GetMapping("/recent")
    public ResponseEntity<List<ReviewResponseDto>> getRecentReviews(@AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(codeReviewService.getRecentReviews(principal.getId()));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteReview(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal principal) {
        codeReviewService.deleteReview(id, principal.getId());
        return ResponseEntity.noContent().build();
    }
}
