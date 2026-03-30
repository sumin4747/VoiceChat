package com.example.conversationAI.user.dto.response;

import java.util.List;

public record UserListResponse(
        List<UserResponse> users
) {
    public static UserListResponse of(List<UserResponse> users) {
        return new UserListResponse(users);
    }
}
