package com.example.codereview.review;

import com.example.codereview.repository.RepositoryEntity;
import com.example.codereview.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {
    List<Review> findByRepositoryOrderByCreatedAtDesc(RepositoryEntity repository);
    List<Review> findByRepositoryIdOrderByCreatedAtDesc(Long repositoryId);
    List<Review> findByUserOrderByCreatedAtDesc(User user);
    List<Review> findByUserIdOrderByCreatedAtDesc(Long userId);
    Optional<Review> findByIdAndUserId(Long id, Long userId);
    Optional<Review> findFirstByRepositoryIdOrderByCreatedAtDesc(Long repositoryId);
    long countByUserId(Long userId);

    @Query("SELECT AVG(r.overallScore) FROM Review r WHERE r.user.id = :userId")
    Double getAverageScoreByUserId(@Param("userId") Long userId);
}
