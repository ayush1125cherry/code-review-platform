package com.example.codereview.chat.dto;

import com.example.codereview.review.dto.CodeReferenceDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatResponseDto {
    private Long conversationId;
    private MessageDto userMessage;
    private MessageDto aiMessage;
    @Builder.Default
    private List<CodeReferenceDto> references = new ArrayList<>();
}
