package com.example.conversationAI.recording.dto.response;

import com.example.conversationAI.recording.domain.RecordingSentence;

public record RecordingSentenceResponse(
        String sentence_id,
        String text
) {
    public static RecordingSentenceResponse from(RecordingSentence s) {
        return new RecordingSentenceResponse(s.getSentenceId(), s.getText());
    }
}