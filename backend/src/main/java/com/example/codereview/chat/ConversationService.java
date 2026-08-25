package com.example.codereview.chat;

import com.example.codereview.chat.dto.*;
import com.example.codereview.exception.ResourceNotFoundException;
import com.example.codereview.exception.UnauthorizedException;
import com.example.codereview.review.Review;
import com.example.codereview.review.ReviewRepository;
import com.example.codereview.review.dto.CodeReferenceDto;
import com.example.codereview.user.User;
import com.example.codereview.user.UserRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ConversationService {

    private static final Logger log = LoggerFactory.getLogger(ConversationService.class);

    private final ConversationRepository conversationRepository;
    private final MessageRepository messageRepository;
    private final ReviewRepository reviewRepository;
    private final UserRepository userRepository;
    private final AiChatService aiChatService;
    private final ObjectMapper objectMapper;

    @Transactional
    public ChatResponseDto processChatMessage(Long reviewId, Long userId, ChatRequestDto request) {
        Review review = reviewRepository.findByIdAndUserId(reviewId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Review not found: " + reviewId));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userId));

        Conversation conversation;
        if (request.getConversationId() != null) {
            conversation = conversationRepository.findByIdAndUserId(request.getConversationId(), userId)
                    .orElseThrow(() -> new ResourceNotFoundException("Conversation not found: " + request.getConversationId()));
        } else {
            conversation = conversationRepository.findFirstByReviewIdOrderByCreatedAtAsc(reviewId)
                    .orElseGet(() -> {
                        Conversation newConv = Conversation.builder()
                                .review(review)
                                .repository(review.getRepository())
                                .user(user)
                                .title("Chat - " + review.getRepository().getName())
                                .build();
                        return conversationRepository.save(newConv);
                    });
        }

        // 1. Save User Message
        Message userMessage = Message.builder()
                .conversation(conversation)
                .sender(MessageSender.USER)
                .content(request.getMessage())
                .referencesJson("[]")
                .build();
        userMessage = messageRepository.save(userMessage);

        // 2. Fetch history
        List<Message> history = messageRepository.findByConversationIdOrderByCreatedAtAsc(conversation.getId());

        // 3. Call AI Chat Service with RAG
        AiChatService.ChatAiResult result = aiChatService.askQuestion(conversation, request.getMessage(), history, user.getGeminiApiKey());

        String refsJson = "[]";
        try {
            refsJson = objectMapper.writeValueAsString(result.references());
        } catch (Exception ignored) {}

        // 4. Save AI Message
        Message aiMessage = Message.builder()
                .conversation(conversation)
                .sender(MessageSender.AI)
                .content(result.responseText())
                .referencesJson(refsJson)
                .build();
        aiMessage = messageRepository.save(aiMessage);

        return ChatResponseDto.builder()
                .conversationId(conversation.getId())
                .userMessage(mapMessageToDto(userMessage))
                .aiMessage(mapMessageToDto(aiMessage))
                .references(result.references())
                .build();
    }

    @Transactional(readOnly = true)
    public ConversationDto getConversationDto(Long conversationId, Long userId) {
        Conversation conversation = conversationRepository.findByIdAndUserId(conversationId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Conversation not found: " + conversationId));
        return mapConversationToDto(conversation);
    }

    @Transactional(readOnly = true)
    public List<ConversationDto> getConversationsByReview(Long reviewId, Long userId) {
        return conversationRepository.findByReviewIdOrderByUpdatedAtDesc(reviewId)
                .stream()
                .filter(c -> c.getUser().getId().equals(userId))
                .map(this::mapConversationToDto)
                .collect(Collectors.toList());
    }

    public ConversationDto mapConversationToDto(Conversation conv) {
        List<Message> messages = messageRepository.findByConversationIdOrderByCreatedAtAsc(conv.getId());
        List<MessageDto> messageDtos = messages.stream()
                .map(this::mapMessageToDto)
                .collect(Collectors.toList());

        return ConversationDto.builder()
                .id(conv.getId())
                .reviewId(conv.getReview().getId())
                .repositoryId(conv.getRepository().getId())
                .repositoryName(conv.getRepository().getName())
                .title(conv.getTitle())
                .messages(messageDtos)
                .createdAt(conv.getCreatedAt())
                .updatedAt(conv.getUpdatedAt())
                .build();
    }

    public MessageDto mapMessageToDto(Message message) {
        List<CodeReferenceDto> refs = new ArrayList<>();
        if (message.getReferencesJson() != null && !message.getReferencesJson().isBlank()) {
            try {
                refs = objectMapper.readValue(message.getReferencesJson(), new TypeReference<List<CodeReferenceDto>>() {});
            } catch (Exception ignored) {}
        }

        return MessageDto.builder()
                .id(message.getId())
                .sender(message.getSender())
                .content(message.getContent())
                .references(refs)
                .createdAt(message.getCreatedAt())
                .build();
    }
}
