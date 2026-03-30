package com.example.conversationAI.voice.dto.response;

import com.example.conversationAI.voice.domain.VoiceModel;

import java.time.LocalDateTime;

public record VoiceListResponse(
        Long voiceId,
        String personName,
        LocalDateTime createdAt,
        VoiceModel.Status status,
        String thumbnailUrl
) {
    public static VoiceListResponse from(VoiceModel model) {
        return new VoiceListResponse(
                model.getId(),
                model.getPersona().getPersonaName(),
                model.getCreatedAt(),
                model.getStatus(),
                model.getThumbnailUrl()
        );
    }
}