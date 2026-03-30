package com.example.conversationAI.user.dto.request;

import jakarta.validation.constraints.NotBlank;

public record UserLoginRequest(
        @NotBlank(message = "loginId는 필수입니다.")
        String loginId,

        @NotBlank(message = "password는 필수입니다.")
        String password
) {}