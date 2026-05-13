package com.example.conversationAI.recording.controller;

import com.example.conversationAI.recording.dto.response.RecordingSentenceResponse;
import com.example.conversationAI.recording.service.RecordingService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
public class RecordingController {

    private final RecordingService recordingService;

    public RecordingController(RecordingService recordingService) {
        this.recordingService = recordingService;
    }

    /** GET /recording-sentences — 녹음 문장 목록 조회 */
    @GetMapping("/recording-sentences")
    public ResponseEntity<List<RecordingSentenceResponse>> getSentences() {
        return ResponseEntity.ok(recordingService.getSentences());
    }

    /** POST /voice-recordings — 문장별 음성 업로드 */
    @PostMapping("/voice-recordings")
    public ResponseEntity<?> uploadRecording(
            @AuthenticationPrincipal Long userId,
            @RequestParam("sentence_id") String sentenceId,
            @RequestParam("audio") MultipartFile audio
    ) {
        recordingService.uploadRecording(userId, sentenceId, audio);
        return ResponseEntity.ok().build();
    }
}