package com.example.conversationAI.persona.service;

import com.example.conversationAI.persona.domain.Persona;
import com.example.conversationAI.persona.dto.request.CreatePersonaRequest;
import com.example.conversationAI.persona.dto.response.PersonaResponse;
import com.example.conversationAI.persona.repository.PersonaRepository;
import com.example.conversationAI.user.repository.UserRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
public class PersonaService {

    private final PersonaRepository personaRepository;
    private final UserRepository userRepository;

    @PersistenceContext
    private EntityManager entityManager;

    public PersonaService(PersonaRepository personaRepository, UserRepository userRepository) {
        this.personaRepository = personaRepository;
        this.userRepository = userRepository;
    }

    public PersonaResponse create(Long userId, CreatePersonaRequest request) {
        ensureUserExists(userId);

        String birthDateStr = request.birthDate() != null ? request.birthDate().toString() : null;
        Persona persona = Persona.create(userId, request.personaName(), birthDateStr);
        Persona saved = personaRepository.save(persona);

        return PersonaResponse.from(saved);
    }

    @Transactional(readOnly = true)
    public List<PersonaResponse> list(Long userId) {
        ensureUserExists(userId);

        return personaRepository.findAllByUserId(userId)
                .stream()
                .map(PersonaResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public PersonaResponse get(Long userId, Long personaId) {
        ensureUserExists(userId);

        Persona persona = personaRepository.findByIdAndUserId(personaId, userId)
                .orElseThrow(() -> new PersonaNotFoundException(personaId));

        return PersonaResponse.from(persona);
    }

    public void delete(Long userId, Long personaId) {
        ensureUserExists(userId);

        Persona persona = personaRepository.findByIdAndUserId(personaId, userId)
                .orElseThrow(() -> new PersonaNotFoundException(personaId));

        persona.softDelete(LocalDateTime.now());
    }

    public Persona createRaw(Long userId, String name, String birthDate) {
        ensureUserExists(userId);
        Persona persona = Persona.create(userId, name, birthDate);
        return personaRepository.save(persona);
    }

    private void ensureUserExists(Long userId) {
        userRepository.findByIdAndDeletedAtIsNull(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));
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
}