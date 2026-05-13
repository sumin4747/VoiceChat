package com.example.conversationAI.responseStyle.service;

import com.example.conversationAI.persona.repository.PersonaRepository;
import com.example.conversationAI.responseStyle.domain.ResponseStyle;
import com.example.conversationAI.responseStyle.dto.request.UpsertResponseStyleRequest;
import com.example.conversationAI.responseStyle.dto.response.ResponseStyleResponse;
import com.example.conversationAI.responseStyle.repository.ResponseStyleRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class ResponseStyleService {

    private final ResponseStyleRepository responseStyleRepository;
    private final PersonaRepository personaRepository;

    public ResponseStyleService(ResponseStyleRepository responseStyleRepository,
                                PersonaRepository personaRepository) {
        this.responseStyleRepository = responseStyleRepository;
        this.personaRepository = personaRepository;
    }

    /** 응답 스타일 저장/변경 (언제든 변경 가능) */
    public ResponseStyleResponse upsert(Long userId, Long personaId, UpsertResponseStyleRequest request) {
        ensurePersonaBelongsToUser(userId, personaId);

        ResponseStyle r = responseStyleRepository.findByPersonaId(personaId)
                .map(existing -> {
                    existing.update(request.style());
                    return existing;
                })
                .orElseGet(() -> ResponseStyle.create(personaId, request.style()));

        return ResponseStyleResponse.from(responseStyleRepository.save(r));
    }

    /** 응답 스타일 조회 */
    @Transactional(readOnly = true)
    public ResponseStyleResponse get(Long userId, Long personaId) {
        ensurePersonaBelongsToUser(userId, personaId);

        ResponseStyle r = responseStyleRepository.findByPersonaId(personaId)
                .orElseThrow(() -> new ResponseStyleNotFoundException(personaId));

        return ResponseStyleResponse.from(r);
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

    public static class ResponseStyleNotFoundException extends RuntimeException {
        public ResponseStyleNotFoundException(Long personaId) {
            super("응답 스타일을 찾을 수 없습니다. personaId=" + personaId);
        }
    }
}