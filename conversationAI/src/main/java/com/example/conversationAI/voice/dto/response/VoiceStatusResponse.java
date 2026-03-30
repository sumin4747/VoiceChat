package com.example.conversationAI.voice.dto.response;

import com.example.conversationAI.voice.domain.VoiceModel;

public record VoiceStatusResponse(
        VoiceModel.Status status,
        int progressPercent
) {}