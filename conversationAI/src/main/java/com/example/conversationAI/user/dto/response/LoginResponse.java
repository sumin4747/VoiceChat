package com.example.conversationAI.user.dto.response;

public record LoginResponse(
        String token,
        boolean isNewUser,
        UserInfo user
) {
    public record UserInfo(Long userId, String nickname) {}
}