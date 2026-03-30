package com.example.conversationAI.consent.dto.response;

import com.example.conversationAI.consent.domain.Consent;
import com.example.conversationAI.consent.domain.ConsentType;

import java.time.LocalDateTime;

public record ConsentResponse(
        Long id,
        Long userId,
        Long personaId,
        ConsentType consentType,
        boolean active,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        LocalDateTime revokedAt
) {
    public static ConsentResponse from(Consent c) {
        return new ConsentResponse(
                c.getId(),
                c.getUserId(),
                c.getPersonaId(),
                c.getConsentType(),
                c.isActive(),
                c.getCreatedAt(),
                c.getUpdatedAt(),
                c.getRevokedAt()
        );
    }
}
