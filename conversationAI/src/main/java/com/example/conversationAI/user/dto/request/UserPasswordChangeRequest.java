package com.example.conversationAI.user.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UserPasswordChangeRequest(
        @NotBlank(message = "currentPassword는 필수입니다.")
        String currentPassword,

        @NotBlank(message = "newPassword는 필수입니다.")
        @Size(min = 8, max = 72, message = "newPassword는 8~72자여야 합니다.")
        String newPassword
) {
    public UserPasswordChangeRequest {
        currentPassword = trimToNull(currentPassword);
        newPassword = trimToNull(newPassword);
    }

    private static String trimToNull(String v) {
        if (v == null) return null;
        String t = v.trim();
        return t.isEmpty() ? null : t;
    }
}