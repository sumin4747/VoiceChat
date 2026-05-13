package com.example.conversationAI.responseStyle.repository;

import com.example.conversationAI.responseStyle.domain.ResponseStyle;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ResponseStyleRepository extends JpaRepository<ResponseStyle, Long> {
    Optional<ResponseStyle> findByPersonaId(Long personaId);
}