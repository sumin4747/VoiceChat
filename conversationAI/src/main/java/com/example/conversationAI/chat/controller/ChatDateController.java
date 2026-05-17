package com.example.conversationAI.chat.controller;

import com.example.conversationAI.chat.domain.ChatMessage;
import com.example.conversationAI.chat.repository.ChatMessageRepository;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/users/voices")
public class ChatDateController {

    private final ChatMessageRepository chatMessageRepository;

    public ChatDateController(ChatMessageRepository chatMessageRepository) {
        this.chatMessageRepository = chatMessageRepository;
    }

    /**
     * GET /users/voices/{voiceId}/messages/date?date=2026-05-16
     * 해당 날짜의 첫 번째 메시지 ID 반환
     */
    @GetMapping("/{voiceId}/messages/date")
    public ResponseEntity<?> getFirstMessageIdByDate(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long voiceId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date
    ) {
        LocalDateTime startOfDay = date.atStartOfDay();
        LocalDateTime endOfDay = date.plusDays(1).atStartOfDay();

        List<ChatMessage> messages = chatMessageRepository.findByVoiceModelIdAndDate(
                voiceId, startOfDay, endOfDay
        );

        if (messages.isEmpty()) {
            return ResponseEntity.ok(Map.of(
                    "messageId", (Object) null,
                    "message", "해당 날짜의 대화가 없습니다."
            ));
        }

        ChatMessage first = messages.get(0);
        return ResponseEntity.ok(Map.of(
                "messageId", first.getId(),
                "date", date.toString(),
                "content", first.getContent()
        ));
    }
}