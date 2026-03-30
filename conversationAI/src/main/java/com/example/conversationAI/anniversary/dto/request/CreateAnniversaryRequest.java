package com.example.conversationAI.anniversary.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record CreateAnniversaryRequest(
        Long personaId,

        @NotBlank(message = "eventName은 필수입니다.")
        @Size(max = 200, message = "eventName은 200자 이하여야 합니다.")
        String eventName,

        @NotNull(message = "eventDate는 필수입니다.")
        LocalDate eventDate,

        Boolean repeatYearly,
        Boolean isEnabled
) {
    public CreateAnniversaryRequest {
        eventName = trimToNull(eventName);
    }

    public boolean repeatYearlyOrDefault() {
        return repeatYearly == null ? true : repeatYearly;
    }

    public boolean enabledOrDefault() {
        return isEnabled == null ? true : isEnabled;
    }

    private static String trimToNull(String v) {
        if (v == null) return null;
        String t = v.trim();
        return t.isEmpty() ? null : t;
    }
}
