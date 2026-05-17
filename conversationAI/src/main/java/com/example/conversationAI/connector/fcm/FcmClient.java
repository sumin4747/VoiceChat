package com.example.conversationAI.connector.fcm;

import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Random;

@Component
public class FcmClient {

    private static final List<String> MESSAGES = List.of(
            "오늘 무슨 일 있었어? 말해줘, 궁금해.",
            "오늘 하루 어땠어? 잠깐 얘기해볼까?",
            "요즘 어때? 잠깐이라도 털어놔봐.",
            "오늘 힘든 일은 없었어? 나한테 말해줘.",
            "오늘 하루도 수고했어. 어떤 하루였어?",
            "잠깐, 오늘 네 하루 들어도 될까?",
            "오늘 기분은 어때? 좋은 것도 나쁜 것도 다 괜찮아.",
            "오늘 뭔가 마음에 걸리는 거 있어?"
    );

    private final Random random = new Random();

    /** 매일 오후 9시 정기 알림 (랜덤 멘트) */
    public void sendNotification(String fcmToken) {
        String messageText = MESSAGES.get(random.nextInt(MESSAGES.size()));
        sendCustomNotification(fcmToken, "안녕, 나야", messageText);
    }

    /** 커스텀 제목/내용 알림 (학습 완료 등) */
    public void sendCustomNotification(String fcmToken, String title, String body) {
        try {
            Message message = Message.builder()
                    .setToken(fcmToken)
                    .setNotification(Notification.builder()
                            .setTitle(title)
                            .setBody(body)
                            .build())
                    .build();

            String response = FirebaseMessaging.getInstance().send(message);
            System.out.println("FCM 발송 성공: " + response);
        } catch (Exception e) {
            System.err.println("FCM 발송 실패: " + e.getMessage());
        }
    }
}
