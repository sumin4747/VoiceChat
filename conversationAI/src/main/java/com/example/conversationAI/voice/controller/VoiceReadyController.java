package com.example.conversationAI.voice.controller;

import com.example.conversationAI.voice.service.VoiceTrainingStatusService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * AI팀이 학습 완료 후 호출하는 내부 API
 * POST /internal/voices/{voiceId}/ready
 * { "modelPath": "/workspace/my_tts_model/checkpoint-epoch-6" }
 */
@RestController
@RequestMapping("/internal/voices")
public class VoiceReadyController {

    private final VoiceTrainingStatusService statusService;

    public VoiceReadyController(VoiceTrainingStatusService statusService) {
        this.statusService = statusService;
    }

    /** 학습 완료 — READY 상태로 변경 */
    @PostMapping("/{voiceId}/ready")
    public ResponseEntity<?> markReady(
            @PathVariable Long voiceId,
            @RequestBody Map<String, String> request
    ) {
        String modelPath = request.get("modelPath");
        if (modelPath == null || modelPath.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("message", "modelPath는 필수입니다."));
        }
        statusService.markReady(voiceId, modelPath);
        return ResponseEntity.ok(Map.of("voiceId", voiceId, "status", "READY"));
    }

    /** 학습 실패 — FAILED 상태로 변경 */
    @PostMapping("/{voiceId}/failed")
    public ResponseEntity<?> markFailed(@PathVariable Long voiceId) {
        statusService.markFailed(voiceId);
        return ResponseEntity.ok(Map.of("voiceId", voiceId, "status", "FAILED"));
    }

    /** 학습 진행률 업데이트 */
    @PostMapping("/{voiceId}/progress")
    public ResponseEntity<?> updateProgress(
            @PathVariable Long voiceId,
            @RequestBody Map<String, Integer> request
    ) {
        int percent = request.getOrDefault("percent", 0);
        statusService.updateProgress(voiceId, percent);
        return ResponseEntity.ok(Map.of("voiceId", voiceId, "progressPercent", percent));
    }
}