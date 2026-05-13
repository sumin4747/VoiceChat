package com.example.conversationAI.user.dto.request;

import jakarta.validation.constraints.NotBlank;

public record FcmTokenRequest(
        @NotBlank(message = "fcmToken은 필수입니다.")
        String fcmToken
) {}