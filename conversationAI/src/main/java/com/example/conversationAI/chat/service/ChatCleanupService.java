package com.example.conversationAI.chat.service;

import com.example.conversationAI.chat.repository.ChatMessageRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class ChatCleanupService {

    private final ChatMessageRepository chatMessageRepository;

    public ChatCleanupService(ChatMessageRepository chatMessageRepository) {
        this.chatMessageRepository = chatMessageRepository;
    }

    /**
     * 매일 새벽 3시에 30일 이전 대화 자동 삭제
     */
    @Scheduled(cron = "0 0 3 * * *", zone = "Asia/Seoul")
    @Transactional
    public void deleteOldMessages() {
        LocalDateTime cutoff = LocalDateTime.now().minusDays(30);
        chatMessageRepository.deleteOlderThan(cutoff);
        System.out.println("30일 이전 대화 삭제 완료: " + cutoff);
    }
}