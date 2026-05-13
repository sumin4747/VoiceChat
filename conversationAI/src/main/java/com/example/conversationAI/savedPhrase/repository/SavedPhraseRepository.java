package com.example.conversationAI.savedPhrase.repository;

import com.example.conversationAI.savedPhrase.domain.SavedPhrase;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SavedPhraseRepository extends JpaRepository<SavedPhrase, Long> {

    List<SavedPhrase> findByVoiceModelIdOrderByCreatedAtDesc(Long voiceModelId);

    Optional<SavedPhrase> findByIdAndVoiceModelId(Long id, Long voiceModelId);
}