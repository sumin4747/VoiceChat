package com.example.conversationAI.persona.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record CreatePersonaRequest(
        @NotBlank(message = "이름은 필수입니다.")
        @Size(max = 100, message = "이름은 100자 이하여야 합니다.")
        String personaName,

        @Size(max = 50, message = "관계는 50자 이하여야 합니다.")
        String relationship,

        @NotNull(message = "생일은 필수입니다.")  // LocalDate는 @NotNull 사용
        LocalDate birthDate
) {
    public CreatePersonaRequest {
        personaName = trimToNull(personaName);
        relationship = trimToNull(relationship);
    }

    private static String trimToNull(String v) {
        if (v == null) return null;
        String t = v.trim();
        return t.isEmpty() ? null : t;
    }
}