package com.example.conversationAI.auth.dto.response;

public record VerifyOtpResponse(
        boolean ok,
        boolean verified,
        String verifyToken
) {}