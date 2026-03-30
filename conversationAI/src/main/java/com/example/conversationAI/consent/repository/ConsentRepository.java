package com.example.conversationAI.consent.repository;

import com.example.conversationAI.consent.domain.Consent;
import com.example.conversationAI.consent.domain.ConsentType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ConsentRepository extends JpaRepository<Consent, Long> {

    Optional<Consent> findByUserIdAndPersonaIdAndConsentType(Long userId, Long personaId, ConsentType consentType);

    List<Consent> findAllByUserIdAndPersonaIdOrderByCreatedAtDesc(Long userId, Long personaId);
}
