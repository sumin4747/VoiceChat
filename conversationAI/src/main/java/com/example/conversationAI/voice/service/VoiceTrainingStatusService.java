package com.example.conversationAI.voice.service;

import com.example.conversationAI.connector.fcm.FcmClient;
import com.example.conversationAI.user.repository.UserRepository;
import com.example.conversationAI.voice.domain.VoiceModel;
import com.example.conversationAI.voice.repository.VoiceModelRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class VoiceTrainingStatusService {

    private final VoiceModelRepository repository;
    private final FcmClient fcmClient;
    private final UserRepository userRepository;

    public VoiceTrainingStatusService(
            VoiceModelRepository repository,
            FcmClient fcmClient,
            UserRepository userRepository
    ) {
        this.repository = repository;
        this.fcmClient = fcmClient;
        this.userRepository = userRepository;
    }

    /** 진행률 업데이트 */
    @Transactional
    public void updateProgress(Long voiceModelId, int percent) {
        VoiceModel model = repository.findById(voiceModelId)
                .orElseThrow(() -> new IllegalArgumentException("VOICE_NOT_FOUND"));
        model.updateProgress(percent);
        System.out.println("[PROGRESS] voiceId=" + voiceModelId + ", percent=" + percent);
    }

    /** 학습 완료 — READY 상태 + FCM 알림 */
    @Transactional
    public void markReady(Long voiceModelId, String externalModelId) {
        VoiceModel model = repository.findById(voiceModelId)
                .orElseThrow(() -> new IllegalArgumentException("VOICE_NOT_FOUND"));
        model.markReady(externalModelId);
        System.out.println("[READY] voiceId=" + voiceModelId);

        // FCM 알림 전송
        Long userId = model.getPersona().getUserId();
        userRepository.findById(userId).ifPresent(user -> {
            if (user.getFcmToken() != null) {
                fcmClient.sendCustomNotification(
                        user.getFcmToken(),
                        "목소리 학습 완료!",
                        "이제 나의 목소리로 대화할 수 있어요."
                );
            }
        });
    }

    /** 학습 실패 — FAILED 상태 */
    @Transactional
    public void markFailed(Long voiceModelId) {
        repository.findById(voiceModelId)
                .ifPresent(VoiceModel::markFailed);
        System.out.println("[FAILED] voiceId=" + voiceModelId);
    }
}