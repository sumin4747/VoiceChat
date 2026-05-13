package com.example.conversationAI.savedPhrase.controller;

import com.example.conversationAI.savedPhrase.dto.request.CreateSavedPhraseRequest;
import com.example.conversationAI.savedPhrase.dto.response.SavedPhraseResponse;
import com.example.conversationAI.savedPhrase.service.SavedPhraseService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/users/voices/{voiceId}/phrases")
public class SavedPhraseController {

    private final SavedPhraseService savedPhraseService;

    public SavedPhraseController(SavedPhraseService savedPhraseService) {
        this.savedPhraseService = savedPhraseService;
    }

    /** POST /users/voices/{voiceId}/phrases — 문장 저장 + TTS 생성 */
    @PostMapping
    public ResponseEntity<SavedPhraseResponse> create(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long voiceId,
            @Valid @RequestBody CreateSavedPhraseRequest request
    ) {
        return ResponseEntity.ok(savedPhraseService.create(voiceId, request));
    }

    /** GET /users/voices/{voiceId}/phrases — 저장 문장 목록 */
    @GetMapping
    public ResponseEntity<List<SavedPhraseResponse>> list(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long voiceId
    ) {
        return ResponseEntity.ok(savedPhraseService.list(voiceId));
    }

    /** POST /users/voices/{voiceId}/phrases/{phraseId}/tts — TTS 재생성 */
    @PostMapping("/{phraseId}/tts")
    public ResponseEntity<SavedPhraseResponse> regenerateTts(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long voiceId,
            @PathVariable Long phraseId
    ) {
        return ResponseEntity.ok(savedPhraseService.regenerateTts(voiceId, phraseId));
    }

    /** DELETE /users/voices/{voiceId}/phrases/{phraseId} — 문장 삭제 */
    @DeleteMapping("/{phraseId}")
    public ResponseEntity<Void> delete(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long voiceId,
            @PathVariable Long phraseId
    ) {
        savedPhraseService.delete(voiceId, phraseId);
        return ResponseEntity.noContent().build();
    }
}