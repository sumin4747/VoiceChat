package com.example.conversationAI.notification.dto.response;

import com.example.conversationAI.notification.domain.Notification;

public record NotificationResponse(
        Long id,
        Long userId,
        int hour,
        int minute,
        boolean enabled
) {
    public static NotificationResponse from(Notification s) {
        return new NotificationResponse(
                s.getId(),
                s.getUserId(),
                s.getNotifyHour(),
                s.getNotifyMinute(),
                s.isEnabled()
        );
    }
}