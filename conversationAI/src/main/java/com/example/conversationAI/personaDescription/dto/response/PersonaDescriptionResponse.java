package com.example.conversationAI.personaDescription.dto.response;

import com.example.conversationAI.personaDescription.domain.PersonaDescription;

public record PersonaDescriptionResponse(
        Long id,
        Long personaId,
        String personaTone,
        String personaPersonality
) {
    public static PersonaDescriptionResponse from(PersonaDescription d) {
        return new PersonaDescriptionResponse(
                d.getId(),
                d.getPersonaId(),
                d.getPersonaTone(),
                d.getPersonaPersonality()
        );
    }
}
