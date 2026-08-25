package com.example.codereview.chat.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatRequestDto {
    private Long conversationId; // optional, if null uses default or creates new
    @NotBlank(message = "Message content cannot be blank")
    private String message;
}
