package com.example.conversationAI.savedPhrase.dto.response;

import com.example.conversationAI.savedPhrase.domain.SavedPhrase;

import java.time.LocalDateTime;

public record SavedPhraseResponse(
        Long id,
        Long voiceModelId,
        String content,
        String audioUrl,
        LocalDateTime createdAt
) {
    public static SavedPhraseResponse from(SavedPhrase p) {
        return new SavedPhraseResponse(
                p.getId(),
                p.getVoiceModelId(),
                p.getContent(),
                p.getAudioUrl(),
                p.getCreatedAt()
        );
    }
}