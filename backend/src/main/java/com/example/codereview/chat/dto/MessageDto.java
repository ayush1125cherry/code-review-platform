package com.example.codereview.chat.dto;

import com.example.codereview.chat.MessageSender;
import com.example.codereview.review.dto.CodeReferenceDto;
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
public class MessageDto {
    private Long id;
    private MessageSender sender;
    private String content;
    @Builder.Default
    private List<CodeReferenceDto> references = new ArrayList<>();
    private LocalDateTime createdAt;
}
