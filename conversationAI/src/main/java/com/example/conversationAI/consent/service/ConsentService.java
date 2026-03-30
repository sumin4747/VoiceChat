package com.example.conversationAI.consent.service;

import com.example.conversationAI.consent.domain.Consent;
import com.example.conversationAI.consent.domain.ConsentType;
import com.example.conversationAI.consent.dto.response.ConsentResponse;
import com.example.conversationAI.consent.repository.ConsentRepository;
import com.example.conversationAI.persona.repository.PersonaRepository;
import com.example.conversationAI.user.repository.UserRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
public class ConsentService {

    private final ConsentRepository consentRepository;
    private final UserRepository userRepository;
    private final PersonaRepository personaRepository;

    public ConsentService(ConsentRepository consentRepository,
                          UserRepository userRepository,
                          PersonaRepository personaRepository) {
        this.consentRepository = consentRepository;
        this.userRepository = userRepository;
        this.personaRepository = personaRepository;
    }

    public ConsentResponse agree(Long userId, Long personaId, ConsentType consentType) {
        ensureUserExists(userId);
        ensurePersonaBelongsToUser(userId, personaId);

        try {
            Consent consent = consentRepository
                    .findByUserIdAndPersonaIdAndConsentType(userId, personaId, consentType)
                    .orElseGet(() -> Consent.create(userId, personaId, consentType));

            consent.agree();

            Consent saved = consentRepository.save(consent);
            return ConsentResponse.from(saved);

        } catch (DataIntegrityViolationException e) {
            Consent existing = consentRepository
                    .findByUserIdAndPersonaIdAndConsentType(userId, personaId, consentType)
                    .orElseThrow(() -> e);

            existing.agree();
            return ConsentResponse.from(existing);
        }
    }

    public ConsentResponse revoke(Long userId, Long personaId, ConsentType consentType) {
        ensureUserExists(userId);
        ensurePersonaBelongsToUser(userId, personaId);

        Consent consent = consentRepository.findByUserIdAndPersonaIdAndConsentType(userId, personaId, consentType)
                .orElseThrow(() -> new ConsentNotFoundException(userId, personaId, consentType));

        consent.revoke(LocalDateTime.now());
        Consent saved = consentRepository.save(consent);
        return ConsentResponse.from(saved);
    }

    @Transactional(readOnly = true)
    public List<ConsentResponse> list(Long userId, Long personaId) {
        ensureUserExists(userId);
        ensurePersonaBelongsToUser(userId, personaId);

        return consentRepository.findAllByUserIdAndPersonaIdOrderByCreatedAtDesc(userId, personaId)
                .stream()
                .map(ConsentResponse::from)
                .toList();
    }

    private void ensureUserExists(Long userId) {
        userRepository.findByIdAndDeletedAtIsNull(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));
    }

    private void ensurePersonaBelongsToUser(Long userId, Long personaId) {
        personaRepository.findByIdAndUserId(personaId, userId)
                .orElseThrow(() -> new PersonaNotFoundException(personaId));
    }

    @Transactional(readOnly = true)
    public void ensureActiveConsent(Long userId, Long personaId, ConsentType type) {

        Consent consent = consentRepository
                .findByUserIdAndPersonaIdAndConsentType(userId, personaId, type)
                .orElseThrow(() ->
                        new IllegalStateException("동의가 필요합니다. type=" + type)
                );

        if (!consent.isActive()) {
            throw new IllegalStateException("철회된 동의입니다. type=" + type);
        }
    }

    public static class UserNotFoundException extends RuntimeException {
        public UserNotFoundException(Long userId) {
            super("사용자를 찾을 수 없습니다. id=" + userId);
        }
    }

    public static class PersonaNotFoundException extends RuntimeException {
        public PersonaNotFoundException(Long personaId) {
            super("페르소나를 찾을 수 없습니다. id=" + personaId);
        }
    }

    public static class ConsentNotFoundException extends RuntimeException {
        public ConsentNotFoundException(Long userId, Long personaId, ConsentType type) {
            super("동의 정보를 찾을 수 없습니다. userId=" + userId + ", personaId=" + personaId + ", consentType=" + type);
        }
    }
}
