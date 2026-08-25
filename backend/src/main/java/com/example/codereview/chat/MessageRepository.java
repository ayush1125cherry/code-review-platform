package com.example.codereview.chat;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MessageRepository extends JpaRepository<Message, Long> {
    List<Message> findByConversationIdOrderByCreatedAtAsc(Long conversationId);

    @Query("SELECT COUNT(m) FROM Message m WHERE m.conversation.user.id = :userId AND m.sender = 'USER'")
    long countUserQuestionsByUserId(@Param("userId") Long userId);
}
