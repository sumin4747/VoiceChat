package com.example.conversationAI.savedPhrase.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateSavedPhraseRequest(
        @NotBlank(message = "content는 필수입니다.")
        @Size(max = 500, message = "문장은 500자 이하여야 합니다.")
        String content
) {}
