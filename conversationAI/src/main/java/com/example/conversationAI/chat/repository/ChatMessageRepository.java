package com.example.conversationAI.chat.repository;

import com.example.conversationAI.chat.domain.ChatMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {

    List<ChatMessage> findByVoiceModelIdOrderByCreatedAtAsc(Long voiceModelId);

    /**
     * 오늘 해당 voiceModel의 AI 응답 수 조회
     */
    @Query("SELECT COUNT(m) FROM ChatMessage m " +
            "WHERE m.voiceModelId = :voiceModelId " +
            "AND m.role = 'AI' " +
            "AND m.createdAt >= :startOfDay")
    long countTodayAiMessages(@Param("voiceModelId") Long voiceModelId,
                              @Param("startOfDay") LocalDateTime startOfDay);

    /**
     * 특정 날짜의 첫 번째 메시지 조회
     */
    @Query("SELECT m FROM ChatMessage m " +
            "WHERE m.voiceModelId = :voiceModelId " +
            "AND m.createdAt >= :startOfDay " +
            "AND m.createdAt < :endOfDay " +
            "ORDER BY m.createdAt ASC")
    List<ChatMessage> findByVoiceModelIdAndDate(
            @Param("voiceModelId") Long voiceModelId,
            @Param("startOfDay") LocalDateTime startOfDay,
            @Param("endOfDay") LocalDateTime endOfDay
    );

    /**
     * 30일 이전 메시지 삭제
     */
    @Modifying
    @Query("DELETE FROM ChatMessage m WHERE m.createdAt < :cutoff")
    void deleteOlderThan(@Param("cutoff") LocalDateTime cutoff);
}