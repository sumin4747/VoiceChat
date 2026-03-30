package com.example.conversationAI.personaDescription.service;

import com.example.conversationAI.persona.repository.PersonaRepository;
import com.example.conversationAI.personaDescription.domain.PersonaDescription;
import com.example.conversationAI.personaDescription.dto.request.UpsertPersonaDescriptionRequest;
import com.example.conversationAI.personaDescription.dto.response.PersonaDescriptionResponse;
import com.example.conversationAI.personaDescription.repository.PersonaDescriptionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class PersonaDescriptionService {

    private final PersonaDescriptionRepository personaDescriptionRepository;
    private final PersonaRepository personaRepository;

    public PersonaDescriptionService(PersonaDescriptionRepository personaDescriptionRepository,
                                     PersonaRepository personaRepository) {
        this.personaDescriptionRepository = personaDescriptionRepository;
        this.personaRepository = personaRepository;
    }

    public PersonaDescriptionResponse upsert(Long userId, Long personaId, UpsertPersonaDescriptionRequest request) {
        ensurePersonaBelongsToUser(userId, personaId);;

        PersonaDescription d = personaDescriptionRepository.findByPersonaId(personaId)
                .map(existing -> {
                    existing.update(request.personaTone(), request.personaPersonality());
                    return existing;
                })
                .orElseGet(() -> PersonaDescription.create(personaId, request.personaTone(), request.personaPersonality()));

        PersonaDescription saved = personaDescriptionRepository.save(d);
        return PersonaDescriptionResponse.from(saved);
    }

    @Transactional(readOnly = true)
    public PersonaDescriptionResponse get(Long userId, Long personaId) {

        ensurePersonaBelongsToUser(userId, personaId);

        PersonaDescription d = personaDescriptionRepository.findByPersonaId(personaId)
                .orElseThrow(() -> new PersonaDescriptionNotFoundException(personaId));

        return PersonaDescriptionResponse.from(d);
    }

    public void delete(Long userId, Long personaId) {

        ensurePersonaBelongsToUser(userId, personaId);

        PersonaDescription d = personaDescriptionRepository.findByPersonaId(personaId)
                .orElseThrow(() -> new PersonaDescriptionNotFoundException(personaId));

        personaDescriptionRepository.delete(d);
    }

    private void ensurePersonaBelongsToUser(Long userId, Long personaId) {
        personaRepository.findByIdAndUserIdAndDeletedAtIsNull(personaId, userId)
                .orElseThrow(() -> new PersonaNotFoundException(personaId));
    }

    public static class PersonaNotFoundException extends RuntimeException {
        public PersonaNotFoundException(Long personaId) {
            super("페르소나를 찾을 수 없습니다. id=" + personaId);
        }
    }

    public static class PersonaDescriptionNotFoundException extends RuntimeException {
        public PersonaDescriptionNotFoundException(Long personaId) {
            super("페르소나 설명을 찾을 수 없습니다. personaId=" + personaId);
        }
    }
}
