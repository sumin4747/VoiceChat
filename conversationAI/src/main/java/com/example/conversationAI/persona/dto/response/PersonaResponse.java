package com.example.conversationAI.persona.dto.response;

import com.example.conversationAI.persona.domain.Persona;

import java.time.LocalDateTime;

public record PersonaResponse(
        Long id,
        Long userId,
        String personaName,
        String relationship,
        LocalDateTime createdAt
) {
    public static PersonaResponse from(Persona persona) {
        return new PersonaResponse(
                persona.getId(),
                persona.getUserId(),
                persona.getPersonaName(),
                persona.getRelationship(),
                persona.getCreatedAt()
        );
    }
}
