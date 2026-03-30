package com.example.conversationAI.voice.service;

import com.example.conversationAI.voice.domain.VoiceModel;
import com.example.conversationAI.voice.repository.VoiceModelRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class VoiceTrainingStatusService {

    private final VoiceModelRepository repository;

    public VoiceTrainingStatusService(VoiceModelRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public void updateProgress(Long voiceModelId, int percent) {
        VoiceModel model = repository.findById(voiceModelId)
                .orElseThrow(() -> new IllegalArgumentException("VOICE_NOT_FOUND"));
        model.updateProgress(percent);
    }

    @Transactional
    public void markReady(Long voiceModelId, String externalModelId) {
        VoiceModel model = repository.findById(voiceModelId)
                .orElseThrow(() -> new IllegalArgumentException("VOICE_NOT_FOUND"));
        model.markReady(externalModelId);
    }

    @Transactional
    public void markFailed(Long voiceModelId) {
        repository.findById(voiceModelId)
                .ifPresent(VoiceModel::markFailed);
    }
}