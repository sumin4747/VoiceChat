package com.example.conversationAI.voice.repository;

import com.example.conversationAI.voice.domain.VoiceModel;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface VoiceModelRepository extends JpaRepository<VoiceModel, Long> {

    List<VoiceModel> findByPersona_Id(Long personaId);

    Optional<VoiceModel> findByIdAndPersona_Id(Long id, Long personaId);

    List<VoiceModel> findByPersona_UserId(Long userId);
}