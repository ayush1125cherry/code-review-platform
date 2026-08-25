package com.example.codereview.chat;

import com.example.codereview.auth.UserPrincipal;
import com.example.codereview.chat.dto.ChatRequestDto;
import com.example.codereview.chat.dto.ChatResponseDto;
import com.example.codereview.chat.dto.ConversationDto;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class ChatController {

    private final ConversationService conversationService;

    @PostMapping("/reviews/{reviewId}/chat")
    public ResponseEntity<ChatResponseDto> sendReviewMessage(
            @PathVariable Long reviewId,
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody ChatRequestDto request) {
        return ResponseEntity.ok(conversationService.processChatMessage(reviewId, principal.getId(), request));
    }

    @GetMapping("/conversations/{id}")
    public ResponseEntity<ConversationDto> getConversation(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(conversationService.getConversationDto(id, principal.getId()));
    }

    @GetMapping("/reviews/{reviewId}/conversations")
    public ResponseEntity<List<ConversationDto>> getConversationsByReview(
            @PathVariable Long reviewId,
            @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(conversationService.getConversationsByReview(reviewId, principal.getId()));
    }
}
