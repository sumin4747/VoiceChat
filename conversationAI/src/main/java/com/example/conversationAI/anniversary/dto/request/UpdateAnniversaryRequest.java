package com.example.conversationAI.anniversary.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record UpdateAnniversaryRequest(
        Long personaId,

        @NotBlank(message = "eventName은 필수입니다.")
        @Size(max = 200, message = "eventName은 200자 이하여야 합니다.")
        String eventName,

        @NotNull(message = "eventDate는 필수입니다.")
        LocalDate eventDate,

        @NotNull(message = "repeatYearly는 필수입니다.")
        Boolean repeatYearly,

        @NotNull(message = "isEnabled는 필수입니다.")
        Boolean isEnabled
) {
    public UpdateAnniversaryRequest {
        eventName = trimToNull(eventName);
    }

    private static String trimToNull(String v) {
        if (v == null) return null;
        String t = v.trim();
        return t.isEmpty() ? null : t;
    }
}
