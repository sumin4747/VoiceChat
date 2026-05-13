package com.example.conversationAI.notification.service;

import com.example.conversationAI.connector.fcm.FcmClient;
import com.example.conversationAI.notification.domain.Notification;
import com.example.conversationAI.notification.dto.request.NotificationRequest;
import com.example.conversationAI.notification.dto.response.NotificationResponse;
import com.example.conversationAI.notification.repository.NotificationRepository;
import com.example.conversationAI.user.repository.UserRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalTime;
import java.util.List;

@Service
@Transactional
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;
    private final FcmClient fcmClient;

    public NotificationService(
            NotificationRepository notificationRepository,
            UserRepository userRepository,
            FcmClient fcmClient
    ) {
        this.notificationRepository = notificationRepository;
        this.userRepository = userRepository;
        this.fcmClient = fcmClient;
    }

    /** 알림 시간 설정/변경 */
    public NotificationResponse upsert(Long userId, NotificationRequest request) {
        Notification setting = notificationRepository.findByUserId(userId)
                .map(existing -> {
                    existing.update(request.hour(), request.minute());
                    return existing;
                })
                .orElseGet(() -> Notification.create(userId, request.hour(), request.minute()));

        return NotificationResponse.from(notificationRepository.save(setting));
    }

    /** 알림 설정 조회 */
    @Transactional(readOnly = true)
    public NotificationResponse get(Long userId) {
        Notification setting = notificationRepository.findByUserId(userId)
                .orElseThrow(() -> new NotificationNotFoundException(userId));
        return NotificationResponse.from(setting);
    }

    /** 알림 끄기 */
    public void disable(Long userId) {
        Notification setting = notificationRepository.findByUserId(userId)
                .orElseThrow(() -> new NotificationNotFoundException(userId));
        setting.disable();
    }

    /** 매분마다 알림 시간 체크 후 FCM 발송 */
    @Scheduled(cron = "0 * * * * *")
    @Transactional(readOnly = true)
    public void sendScheduledNotifications() {
        LocalTime now = LocalTime.now();
        int hour = now.getHour();
        int minute = now.getMinute();

        List<Notification> targets = notificationRepository.findAllByTime(hour, minute);

        for (Notification setting : targets) {
            userRepository.findById(setting.getUserId()).ifPresent(user -> {
                if (user.getFcmToken() != null) {
                    fcmClient.sendNotification(user.getFcmToken());
                }
            });
        }

        if (!targets.isEmpty()) {
            System.out.println("FCM 발송 완료: " + targets.size() + "명");
        }
    }

    public static class NotificationNotFoundException extends RuntimeException {
        public NotificationNotFoundException(Long userId) {
            super("알림 설정을 찾을 수 없습니다. userId=" + userId);
        }
    }
}