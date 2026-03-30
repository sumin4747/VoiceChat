package com.example.conversationAI.consent.dto.request;

import com.example.conversationAI.consent.domain.ConsentType;
import jakarta.validation.constraints.NotNull;

public record UpsertConsentRequest(
        @NotNull(message = "consentType은 필수입니다.")
        ConsentType consentType
) {
}
