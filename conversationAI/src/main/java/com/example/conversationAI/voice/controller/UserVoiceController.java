package com.example.conversationAI.voice.controller;

import com.example.conversationAI.chat.service.ChatService;
import com.example.conversationAI.connector.stt.WhisperClient;
import com.example.conversationAI.persona.domain.Persona;
import com.example.conversationAI.persona.service.PersonaService;
import com.example.conversationAI.voice.domain.VoiceModel;
import com.example.conversationAI.voice.service.VoiceModelService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/users/voices")
public class UserVoiceController {

    private final PersonaService personaService;
    private final VoiceModelService voiceModelService;
    private final ChatService chatService;
    private final WhisperClient whisperClient;

    public UserVoiceController(
            PersonaService personaService,
            VoiceModelService voiceModelService,
            ChatService chatService,
            WhisperClient whisperClient
    ) {
        this.personaService = personaService;
        this.voiceModelService = voiceModelService;
        this.chatService = chatService;
        this.whisperClient = whisperClient;
    }

    /** POST /users/voices — 보이스 모델 생성 */
    @PostMapping
    public ResponseEntity<?> create(
            @AuthenticationPrincipal Long userId,
            @RequestBody Map<String, String> request
    ) {
        Persona persona = personaService.createRaw(userId, request.get("personName"), request.get("birthDate"));
        VoiceModel model = voiceModelService.create(persona.getId(), "qwen3");

        return ResponseEntity.ok(Map.of(
                "voiceId", model.getId(),
                "status", model.getStatus()
        ));
    }

    /** GET /users/voices/{voiceId}/status — 학습 상태 확인 */
    @GetMapping("/{voiceId}/status")
    public ResponseEntity<?> status(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long voiceId
    ) {
        validateOwnership(userId, voiceId);
        VoiceModel model = voiceModelService.getById(voiceId);

        return ResponseEntity.ok(Map.of(
                "status", model.getStatus(),
                "progressPercent", model.getProgressPercent()
        ));
    }

    /** GET /users/voices — 보이스 모델 목록 */
    @GetMapping
    public ResponseEntity<?> list(@AuthenticationPrincipal Long userId) {
        List<Map<String, Object>> response = voiceModelService.listByUser(userId).stream()
                .map(model -> Map.<String, Object>of(
                        "voiceId",     model.getId(),
                        "personName",  model.getPersona().getPersonaName(),
                        "createdAt",   model.getCreatedAt(),
                        "status",      model.getStatus()
                ))
                .collect(Collectors.toList());

        return ResponseEntity.ok(response);
    }

    /** POST /users/voices/{voiceId}/stt — 음성 → 텍스트 변환만 반환 */
    @PostMapping("/{voiceId}/stt")
    public ResponseEntity<?> stt(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long voiceId,
            @RequestParam("file") MultipartFile audioFile
    ) {
        validateOwnership(userId, voiceId);
        String transcribed = whisperClient.transcribe(audioFile);

        return ResponseEntity.ok(Map.of(
                "userMessage", transcribed
        ));
    }

    /** POST /users/voices/{voiceId}/chat — 텍스트 채팅 (LLM 응답 + TTS) */
    @PostMapping("/{voiceId}/chat")
    public ResponseEntity<?> chat(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long voiceId,
            @RequestBody Map<String, String> request
    ) {
        validateOwnership(userId, voiceId);
        ChatService.ChatResult result = chatService.chat(voiceId, request.get("message"));

        return ResponseEntity.ok(Map.of(
                "replyText",   result.replyText(),
                "ttsAudioUrl", result.ttsAudioUrl() != null ? result.ttsAudioUrl() : ""
        ));
    }

    /** GET /users/voices/{voiceId}/messages — 채팅 기록 */
    @GetMapping("/{voiceId}/messages")
    public ResponseEntity<?> history(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long voiceId
    ) {
        validateOwnership(userId, voiceId);

        List<Map<String, Object>> response = chatService.history(voiceId).stream()
                .map(msg -> Map.<String, Object>of(
                        "role",      msg.getRole().name().toLowerCase(),
                        "content",   msg.getContent(),
                        "audioUrl",  msg.getAudioUrl() != null ? msg.getAudioUrl() : "",
                        "createdAt", msg.getCreatedAt()
                ))
                .collect(Collectors.toList());

        return ResponseEntity.ok(response);
    }

    private void validateOwnership(Long userId, Long voiceId) {
        VoiceModel model = voiceModelService.getById(voiceId);
        Long ownerId = model.getPersona().getUserId();
        if (!ownerId.equals(userId)) {
            throw new SecurityException("해당 voiceId에 접근 권한 없음");
        }
    }
}