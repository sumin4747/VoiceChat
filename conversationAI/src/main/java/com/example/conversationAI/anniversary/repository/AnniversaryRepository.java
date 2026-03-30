package com.example.conversationAI.anniversary.repository;

import com.example.conversationAI.anniversary.domain.Anniversary;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AnniversaryRepository extends JpaRepository<Anniversary, Long> {

    List<Anniversary> findAllByUserIdOrderByEventDateAsc(Long userId);

    List<Anniversary> findAllByUserIdAndPersonaIdOrderByEventDateAsc(Long userId, Long personaId);

    Optional<Anniversary> findByIdAndUserId(Long id, Long userId);
}
