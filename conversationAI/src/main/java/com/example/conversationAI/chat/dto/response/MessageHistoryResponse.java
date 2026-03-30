package com.example.conversationAI.chat.dto.response;

import com.example.conversationAI.chat.domain.ChatMessage;

import java.time.LocalDateTime;

public record MessageHistoryResponse(
        String role,
        String content,
        String audioUrl,
        LocalDateTime createdAt
) {
    public static MessageHistoryResponse from(ChatMessage msg) {
        return new MessageHistoryResponse(
                msg.getRole().name(),
                msg.getContent(),
                msg.getRole() == ChatMessage.Role.AI ? msg.getAudioUrl() : null,
                msg.getCreatedAt()
        );
    }
}