package com.example.codereview.chat;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ConversationRepository extends JpaRepository<Conversation, Long> {
    List<Conversation> findByReviewIdOrderByUpdatedAtDesc(Long reviewId);
    List<Conversation> findByRepositoryIdOrderByUpdatedAtDesc(Long repositoryId);
    List<Conversation> findByUserIdOrderByUpdatedAtDesc(Long userId);
    Optional<Conversation> findByIdAndUserId(Long id, Long userId);
    Optional<Conversation> findFirstByReviewIdOrderByCreatedAtAsc(Long reviewId);
    long countByUserId(Long userId);
}
