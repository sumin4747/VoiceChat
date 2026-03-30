package com.example.conversationAI.user.dto.response;

import com.example.conversationAI.user.domain.User;
import com.example.conversationAI.user.domain.UserStatus;

import java.time.LocalDateTime;

public record UserResponse(
        Long id,
        String email,
        String nickname,
        UserStatus status,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static UserResponse from(User user) {
        return new UserResponse(
                user.getId(),
                user.getEmail(),
                user.getNickname(),
                user.getStatus(),
                user.getCreatedAt(),
                user.getUpdatedAt()
        );
    }
}
