package com.example.codereview.review;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReviewFindingRepository extends JpaRepository<ReviewFinding, Long> {
    List<ReviewFinding> findByReviewId(Long reviewId);
    List<ReviewFinding> findByReviewIdAndCategory(Long reviewId, FindingCategory category);
}
