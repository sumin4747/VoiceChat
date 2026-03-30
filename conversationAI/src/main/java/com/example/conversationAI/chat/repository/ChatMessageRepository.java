package com.example.conversationAI.chat.repository;

import com.example.conversationAI.chat.domain.ChatMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {

    List<ChatMessage> findByVoiceModelIdOrderByCreatedAtAsc(Long voiceModelId);

    /**
     * 오늘 해당 voiceModel의 AI 응답 수 조회 (일일 대화 횟수 제한용)
     */
    @Query("SELECT COUNT(m) FROM ChatMessage m " +
            "WHERE m.voiceModelId = :voiceModelId " +
            "AND m.role = 'AI' " +
            "AND m.createdAt >= :startOfDay")
    long countTodayAiMessages(@Param("voiceModelId") Long voiceModelId,
                              @Param("startOfDay") LocalDateTime startOfDay);
}