package com.example.conversationAI.voice.service;

import com.example.conversationAI.connector.runpod.RunPodClient;
import com.example.conversationAI.persona.domain.Persona;
import com.example.conversationAI.persona.repository.PersonaRepository;
import com.example.conversationAI.voice.domain.VoiceModel;
import com.example.conversationAI.voice.repository.VoiceModelRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Transactional
public class VoiceModelService {

    private final VoiceModelRepository voiceModelRepository;
    private final PersonaRepository personaRepository;
    private final RunPodClient runPodClient;

    public VoiceModelService(
            VoiceModelRepository voiceModelRepository,
            PersonaRepository personaRepository,
            RunPodClient runPodClient
    ) {
        this.voiceModelRepository = voiceModelRepository;
        this.personaRepository = personaRepository;
        this.runPodClient = runPodClient;
    }

    /**
     * VoiceModel 생성 + RunPod 학습 시작 요청
     * status = TRAINING, progress = 0 으로 저장
     */
    public VoiceModel create(Long personaId, String provider) {
        Persona persona = personaRepository.findById(personaId)
                .orElseThrow(() -> new IllegalArgumentException("PERSONA_NOT_FOUND"));

        VoiceModel model = VoiceModel.create(persona, provider);
        VoiceModel saved = voiceModelRepository.save(model);

        // RunPod에 비동기 학습 시작 요청
        runPodClient.startTraining(saved.getId(), persona.getUserId());

        return saved;
    }

    public VoiceModel get(Long personaId, Long modelId) {
        return voiceModelRepository
                .findByIdAndPersona_Id(modelId, personaId)
                .orElseThrow(() -> new IllegalArgumentException("VOICE_MODEL_NOT_FOUND"));
    }

    public VoiceModel getById(Long id) {
        return voiceModelRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("VOICE_NOT_FOUND"));
    }

    public List<VoiceModel> listByUser(Long userId) {
        return voiceModelRepository.findByPersona_UserId(userId);
    }

    public List<VoiceModel> list(Long personaId) {
        return voiceModelRepository.findByPersona_Id(personaId);
    }
}