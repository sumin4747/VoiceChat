package com.example.conversationAI.voice.controller;

import com.example.conversationAI.chat.domain.ChatMessage;
import com.example.conversationAI.chat.service.ChatService;
import com.example.conversationAI.persona.domain.Persona;
import com.example.conversationAI.persona.service.PersonaService;
import com.example.conversationAI.voice.domain.VoiceModel;
import com.example.conversationAI.voice.service.VoiceModelService;
import com.example.conversationAI.voice.service.VoiceTrainingService;
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
    private final VoiceTrainingService trainingService;
    private final ChatService chatService;

    public UserVoiceController(
            PersonaService personaService,
            VoiceModelService voiceModelService,
            VoiceTrainingService trainingService,
            ChatService chatService
    ) {
        this.personaService = personaService;
        this.voiceModelService = voiceModelService;
        this.trainingService = trainingService;
        this.chatService = chatService;
    }

    @PostMapping
    public ResponseEntity<?> create(
            @AuthenticationPrincipal Long userId,
            @RequestBody Map<String, String> request
    ) {
        Persona persona = personaService.createRaw(userId, request.get("personName"), request.get("birthDate"));
        VoiceModel model = voiceModelService.create(persona.getId(), "zonos");

        return ResponseEntity.ok(Map.of(
                "voiceId", model.getId(),
                "status", model.getStatus()
        ));
    }

    @PostMapping("/{voiceId}/upload")
    public ResponseEntity<?> upload(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long voiceId,
            @RequestParam("files") List<MultipartFile> files
    ) {
        validateOwnership(userId, voiceId);

        if (files == null || files.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of(
                    "code", "BAD_REQUEST",
                    "message", "파일을 1개 이상 업로드해야 합니다."
            ));
        }

        trainingService.startTraining(voiceId, files);

        return ResponseEntity.ok(Map.of("voiceId", voiceId));
    }

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

    @GetMapping
    public ResponseEntity<?> list(@AuthenticationPrincipal Long userId) {
        List<Map<String, Object>> response = voiceModelService.listByUser(userId).stream()
                .map(model -> Map.<String, Object>of(
                        "voiceId",     model.getId(),
                        "personName",  model.getPersona().getPersonaName(),
                        "createdAt",   model.getCreatedAt(),
                        "status",      model.getStatus(),
                        "thumbnailUrl", (Object) null
                ))
                .collect(Collectors.toList());

        return ResponseEntity.ok(response);
    }

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

    @PostMapping("/{voiceId}/chat/voice")
    public ResponseEntity<?> chatWithVoice(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long voiceId,
            @RequestParam("file") MultipartFile audioFile
    ) {
        validateOwnership(userId, voiceId);
        ChatService.ChatResult result = chatService.chatWithVoice(voiceId, audioFile);

        return ResponseEntity.ok(Map.of(
                "replyText",   result.replyText(),
                "ttsAudioUrl", result.ttsAudioUrl() != null ? result.ttsAudioUrl() : ""
        ));
    }

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