package com.example.conversationAI.notification.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record NotificationRequest(
        @NotNull(message = "hour은 필수입니다.")
        @Min(value = 0, message = "hour은 0~23 사이여야 합니다.")
        @Max(value = 23, message = "hour은 0~23 사이여야 합니다.")
        Integer hour,

        @NotNull(message = "minute은 필수입니다.")
        @Min(value = 0, message = "minute은 0~59 사이여야 합니다.")
        @Max(value = 59, message = "minute은 0~59 사이여야 합니다.")
        Integer minute
) {}