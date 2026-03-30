package com.example.conversationAI.chat.dto.response;

public record ChatResponse(
        String replyText,
        String ttsAudioUrl  // TTS 연동 전까지 null
) {}