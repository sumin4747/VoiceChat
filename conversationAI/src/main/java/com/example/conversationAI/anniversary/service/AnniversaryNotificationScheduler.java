package com.example.conversationAI.anniversary.service;

import com.example.conversationAI.anniversary.domain.Anniversary;
import com.example.conversationAI.anniversary.repository.AnniversaryRepository;
import com.example.conversationAI.chat.repository.ChatMessageRepository;
import com.example.conversationAI.connector.fcm.FcmClient;
import com.example.conversationAI.user.repository.UserRepository;
import com.example.conversationAI.voice.domain.VoiceModel;
import com.example.conversationAI.voice.repository.VoiceModelRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
public class AnniversaryNotificationScheduler {

    private final AnniversaryRepository anniversaryRepository;
    private final VoiceModelRepository voiceModelRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final UserRepository userRepository;
    private final FcmClient fcmClient;

    /** 기념일 알림 — 매일 오전 9시 */
    @Scheduled(cron = "0 0 9 * * *")
    public void sendAnniversaryNotifications() {
        LocalDate today = LocalDate.now();
        List<Anniversary> targets = anniversaryRepository.findTodayAnniversaries(today);

        for (Anniversary a : targets) {
            userRepository.findByIdAndDeletedAtIsNull(a.getUserId()).ifPresent(user -> {
                if (user.getFcmToken() != null) {
                    fcmClient.send(
                            user.getFcmToken(),
                            a.getEventName(),
                            "오늘은 " + a.getEventName() + "이에요. 대화해보시겠어요?"
                    );
                }
            });
        }
    }

    /** 비활성 알림 — 매일 오전 10시 */
    @Scheduled(cron = "0 0 10 * * *")
    public void sendInactivityReminders() {
        List<VoiceModel> targets = voiceModelRepository.findAllWithReminderEnabled();

        for (VoiceModel model : targets) {
            int intervalDays = model.getReminderIntervalDays();
            LocalDateTime threshold = LocalDateTime.now().minusDays(intervalDays);

            chatMessageRepository.findLastMessageTime(model.getId()).ifPresentOrElse(
                    lastTime -> {
                        if (lastTime.isBefore(threshold)) {
                            sendReminder(model);
                        }
                    },
                    () -> sendReminder(model) // 대화 기록 자체가 없을 때
            );
        }
    }

    private void sendReminder(VoiceModel model) {
        Long userId = model.getPersona().getUserId();
        userRepository.findByIdAndDeletedAtIsNull(userId).ifPresent(user -> {
            if (user.getFcmToken() != null) {
                fcmClient.send(
                        user.getFcmToken(),
                        model.getPersona().getPersonaName(),
                        "오랜만이에요. 대화해보시겠어요?"
                );
            }
        });
    }
}