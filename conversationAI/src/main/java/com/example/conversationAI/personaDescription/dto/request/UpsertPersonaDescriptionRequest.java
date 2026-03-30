package com.example.conversationAI.personaDescription.dto.request;

import jakarta.validation.constraints.NotBlank;

public record UpsertPersonaDescriptionRequest(
        @NotBlank(message = "personaTone은 필수입니다.")
        String personaTone,
        String personaPersonality
) {
    public UpsertPersonaDescriptionRequest {
        personaTone = trimToNull(personaTone);
        personaPersonality = trimToNull(personaPersonality);
    }

    private static String trimToNull(String v) {
        if (v == null) return null;
        String t = v.trim();
        return t.isEmpty() ? null : t;
    }
}
