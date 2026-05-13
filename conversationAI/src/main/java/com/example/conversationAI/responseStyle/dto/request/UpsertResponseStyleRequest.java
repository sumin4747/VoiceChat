package com.example.conversationAI.responseStyle.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record UpsertResponseStyleRequest(
        @NotBlank(message = "style은 필수입니다.")
        @Pattern(
                regexp = "EMPATHY|ADVICE|SUMMARY|BRIEF",
                message = "style은 EMPATHY, ADVICE, SUMMARY, BRIEF 중 하나여야 합니다."
        )
        String style
) {}