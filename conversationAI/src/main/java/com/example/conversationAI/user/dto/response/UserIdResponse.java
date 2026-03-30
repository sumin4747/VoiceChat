package com.example.conversationAI.user.dto.response;

public record UserIdResponse(
        Long id
) {
    public static UserIdResponse from(Long id) {
        return new UserIdResponse(id);
    }
}
