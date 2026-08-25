package com.example.codereview.chat.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConversationDto {
    private Long id;
    private Long reviewId;
    private Long repositoryId;
    private String repositoryName;
    private String title;
    @Builder.Default
    private List<MessageDto> messages = new ArrayList<>();
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
