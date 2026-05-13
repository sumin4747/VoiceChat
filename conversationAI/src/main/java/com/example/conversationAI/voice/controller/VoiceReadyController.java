package com.example.conversationAI.voice.controller;

import com.example.conversationAI.voice.service.VoiceTrainingStatusService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/internal/voices")
public class VoiceReadyController {

    private final VoiceTrainingStatusService statusService;

    public VoiceReadyController(VoiceTrainingStatusService statusService) {
        this.statusService = statusService;
    }

    /** AI팀 학습 완료 후 호출 — READY 상태로 변경 */
    @PostMapping("/{voiceId}/ready")
    public ResponseEntity<?> markReady(
            @PathVariable Long voiceId,
            @RequestBody(required = false) Map<String, Object> body
    ) {
        String modelPath = body != null ? (String) body.get("modelPath") : null;
        statusService.markReady(voiceId, modelPath != null ? modelPath : "");
        System.out.println("[READY] voiceId=" + voiceId + ", modelPath=" + modelPath);
        return ResponseEntity.ok(Map.of("voiceId", voiceId, "status", "READY"));
    }

    /** AI팀 학습 실패 후 호출 — FAILED 상태로 변경 */
    @PostMapping("/{voiceId}/failed")
    public ResponseEntity<?> markFailed(@PathVariable Long voiceId) {
        statusService.markFailed(voiceId);
        System.out.println("[FAILED] voiceId=" + voiceId);
        return ResponseEntity.ok(Map.of("voiceId", voiceId, "status", "FAILED"));
    }

    /** AI팀 학습 진행률 업데이트 */
    @PostMapping("/{voiceId}/progress")
    public ResponseEntity<?> updateProgress(
            @PathVariable Long voiceId,
            @RequestBody(required = false) Map<String, Object> body
    ) {
        int percent = body != null ? (int) body.getOrDefault("percent", 0) : 0;
        statusService.updateProgress(voiceId, percent);
        System.out.println("[PROGRESS] voiceId=" + voiceId + ", percent=" + percent);
        return ResponseEntity.ok(Map.of("voiceId", voiceId, "progressPercent", percent));
    }
}