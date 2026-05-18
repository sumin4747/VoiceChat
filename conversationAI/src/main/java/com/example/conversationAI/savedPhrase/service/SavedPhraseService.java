package com.example.conversationAI.savedPhrase.service;

import com.example.conversationAI.common.storage.LocalFileStorage;
import com.example.conversationAI.connector.tts.TtsClient;
import com.example.conversationAI.savedPhrase.domain.SavedPhrase;
import com.example.conversationAI.savedPhrase.dto.request.CreateSavedPhraseRequest;
import com.example.conversationAI.savedPhrase.dto.response.SavedPhraseResponse;
import com.example.conversationAI.savedPhrase.repository.SavedPhraseRepository;
import com.example.conversationAI.voice.domain.VoiceModel;
import com.example.conversationAI.voice.repository.VoiceModelRepository;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class SavedPhraseService {

    private final SavedPhraseRepository savedPhraseRepository;
    private final VoiceModelRepository voiceModelRepository;
    private final TtsClient ttsClient;
    private final LocalFileStorage fileStorage;

    public SavedPhraseService(
            SavedPhraseRepository savedPhraseRepository,
            VoiceModelRepository voiceModelRepository,
            @Qualifier("qwen3TtsClient") TtsClient ttsClient,
            LocalFileStorage fileStorage
    ) {
        this.savedPhraseRepository = savedPhraseRepository;
        this.voiceModelRepository = voiceModelRepository;
        this.ttsClient = ttsClient;
        this.fileStorage = fileStorage;
    }

    public SavedPhraseResponse create(Long voiceModelId, CreateSavedPhraseRequest request) {
        VoiceModel voiceModel = getVoiceModel(voiceModelId);

        SavedPhrase phrase = SavedPhrase.create(voiceModelId, request.content());
        savedPhraseRepository.save(phrase);

        String audioUrl = generateTts(voiceModel, request.content());
        if (audioUrl != null) {
            phrase.updateAudioUrl(audioUrl);
        }

        return SavedPhraseResponse.from(phrase);
    }

    @Transactional(readOnly = true)
    public List<SavedPhraseResponse> list(Long voiceModelId) {
        return savedPhraseRepository
                .findByVoiceModelIdOrderByCreatedAtDesc(voiceModelId)
                .stream()
                .map(SavedPhraseResponse::from)
                .toList();
    }

    public SavedPhraseResponse regenerateTts(Long voiceModelId, Long phraseId) {
        VoiceModel voiceModel = getVoiceModel(voiceModelId);

        SavedPhrase phrase = savedPhraseRepository
                .findByIdAndVoiceModelId(phraseId, voiceModelId)
                .orElseThrow(() -> new PhraseNotFoundException(phraseId));

        String audioUrl = generateTts(voiceModel, phrase.getContent());
        if (audioUrl != null) {
            phrase.updateAudioUrl(audioUrl);
        }

        return SavedPhraseResponse.from(phrase);
    }

    public void delete(Long voiceModelId, Long phraseId) {
        SavedPhrase phrase = savedPhraseRepository
                .findByIdAndVoiceModelId(phraseId, voiceModelId)
                .orElseThrow(() -> new PhraseNotFoundException(phraseId));

        savedPhraseRepository.delete(phrase);
    }

    private String generateTts(VoiceModel voiceModel, String content) {
        if (voiceModel.getStatus() != VoiceModel.Status.READY) return null;
        try {
            String modelPath = voiceModel.getExternalModelId();
            byte[] audioBytes = ttsClient.synthesize(content, null, "Warm and gentle tone.", modelPath);
            return fileStorage.uploadTtsResult(voiceModel.getId(), audioBytes, "wav");
        } catch (Exception e) {
            System.err.println("저장 문장 TTS 생성 실패: " + e.getMessage());
            return null;
        }
    }

    private VoiceModel getVoiceModel(Long voiceModelId) {
        return voiceModelRepository.findById(voiceModelId)
                .orElseThrow(() -> new IllegalArgumentException("VoiceModel 없음: " + voiceModelId));
    }

    public static class PhraseNotFoundException extends RuntimeException {
        public PhraseNotFoundException(Long phraseId) {
            super("저장된 문장을 찾을 수 없습니다. id=" + phraseId);
        }
    }
}