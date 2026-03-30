package com.example.conversationAI.anniversary.dto.response;

import com.example.conversationAI.anniversary.domain.Anniversary;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record AnniversaryResponse(
        Long id,
        Long userId,
        Long personaId,
        String eventName,
        LocalDate eventDate,
        boolean repeatYearly,
        boolean isEnabled,
        LocalDateTime createdAt
) {
    public static AnniversaryResponse from(Anniversary a) {
        return new AnniversaryResponse(
                a.getId(),
                a.getUserId(),
                a.getPersonaId(),
                a.getEventName(),
                a.getEventDate(),
                a.isRepeatYearly(),
                a.isEnabled(),
                a.getCreatedAt()
        );
    }
}
