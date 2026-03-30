package com.example.conversationAI.anniversary.dto.request;

import jakarta.validation.constraints.NotNull;

public record UpdateAnniversaryEnabledRequest(
        @NotNull(message = "isEnabled는 필수입니다.")
        Boolean isEnabled
) {
}
