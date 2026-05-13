package com.example.conversationAI.responseStyle.dto.response;

import com.example.conversationAI.responseStyle.domain.ResponseStyle;

public record ResponseStyleResponse(
        Long id,
        Long personaId,
        String style
) {
    public static ResponseStyleResponse from(ResponseStyle r) {
        return new ResponseStyleResponse(
                r.getId(),
                r.getPersonaId(),
                r.getStyle()
        );
    }
}