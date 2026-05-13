package com.example.conversationAI.notification.controller;

import com.example.conversationAI.notification.dto.request.NotificationRequest;
import com.example.conversationAI.notification.dto.response.NotificationResponse;
import com.example.conversationAI.notification.service.NotificationService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/users/notification")
public class NotificationController {

    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    /** PUT /users/notification — 알림 시간 설정/변경 */
    @PutMapping
    public ResponseEntity<NotificationResponse> upsert(
            @AuthenticationPrincipal Long userId,
            @Valid @RequestBody NotificationRequest request
    ) {
        return ResponseEntity.ok(notificationService.upsert(userId, request));
    }

    /** GET /users/notification — 알림 설정 조회 */
    @GetMapping
    public ResponseEntity<NotificationResponse> get(
            @AuthenticationPrincipal Long userId
    ) {
        return ResponseEntity.ok(notificationService.get(userId));
    }

    /** DELETE /users/notification — 알림 끄기 */
    @DeleteMapping
    public ResponseEntity<Void> disable(
            @AuthenticationPrincipal Long userId
    ) {
        notificationService.disable(userId);
        return ResponseEntity.noContent().build();
    }
}