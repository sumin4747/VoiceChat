package com.example.conversationAI.anniversary.service;

import com.example.conversationAI.anniversary.domain.Anniversary;
import com.example.conversationAI.anniversary.dto.request.CreateAnniversaryRequest;
import com.example.conversationAI.anniversary.dto.request.UpdateAnniversaryEnabledRequest;
import com.example.conversationAI.anniversary.dto.request.UpdateAnniversaryRequest;
import com.example.conversationAI.anniversary.dto.response.AnniversaryResponse;
import com.example.conversationAI.anniversary.repository.AnniversaryRepository;
import com.example.conversationAI.persona.repository.PersonaRepository;
import com.example.conversationAI.user.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class AnniversaryService {

    private final AnniversaryRepository anniversaryRepository;
    private final UserRepository userRepository;
    private final PersonaRepository personaRepository;

    public AnniversaryService(AnniversaryRepository anniversaryRepository,
                              UserRepository userRepository,
                              PersonaRepository personaRepository) {
        this.anniversaryRepository = anniversaryRepository;
        this.userRepository = userRepository;
        this.personaRepository = personaRepository;
    }

    public AnniversaryResponse create(Long userId, CreateAnniversaryRequest request) {
        ensureUserExists(userId);

        Long personaId = request.personaId();
        if (personaId != null) {
            ensurePersonaBelongsToUser(userId, personaId);
        }

        Anniversary a = Anniversary.create(
                userId,
                personaId,
                request.eventName(),
                request.eventDate(),
                request.repeatYearlyOrDefault(),
                request.enabledOrDefault()
        );

        Anniversary saved = anniversaryRepository.save(a);
        return AnniversaryResponse.from(saved);
    }

    @Transactional(readOnly = true)
    public List<AnniversaryResponse> list(Long userId, Long personaId) {
        ensureUserExists(userId);

        List<Anniversary> list;
        if (personaId == null) {
            list = anniversaryRepository.findAllByUserIdOrderByEventDateAsc(userId);
        } else {
            ensurePersonaBelongsToUser(userId, personaId);
            list = anniversaryRepository.findAllByUserIdAndPersonaIdOrderByEventDateAsc(userId, personaId);
        }

        return list.stream().map(AnniversaryResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public AnniversaryResponse get(Long userId, Long anniversaryId) {
        ensureUserExists(userId);

        Anniversary a = anniversaryRepository.findByIdAndUserId(anniversaryId, userId)
                .orElseThrow(() -> new AnniversaryNotFoundException(anniversaryId));

        return AnniversaryResponse.from(a);
    }

    public AnniversaryResponse update(Long userId, Long anniversaryId, UpdateAnniversaryRequest request) {
        ensureUserExists(userId);

        Anniversary a = anniversaryRepository.findByIdAndUserId(anniversaryId, userId)
                .orElseThrow(() -> new AnniversaryNotFoundException(anniversaryId));

        Long personaId = request.personaId();
        if (personaId != null) {
            ensurePersonaBelongsToUser(userId, personaId);
        }

        a.update(
                personaId,
                request.eventName(),
                request.eventDate(),
                request.repeatYearly(),
                request.isEnabled()
        );

        return AnniversaryResponse.from(a);
    }

    public AnniversaryResponse updateEnabled(Long userId, Long anniversaryId, UpdateAnniversaryEnabledRequest request) {
        ensureUserExists(userId);

        Anniversary a = anniversaryRepository.findByIdAndUserId(anniversaryId, userId)
                .orElseThrow(() -> new AnniversaryNotFoundException(anniversaryId));

        a.changeEnabled(request.isEnabled());
        return AnniversaryResponse.from(a);
    }

    public void delete(Long userId, Long anniversaryId) {
        ensureUserExists(userId);

        Anniversary a = anniversaryRepository.findByIdAndUserId(anniversaryId, userId)
                .orElseThrow(() -> new AnniversaryNotFoundException(anniversaryId));

        anniversaryRepository.delete(a);
    }

    private void ensureUserExists(Long userId) {
        userRepository.findByIdAndDeletedAtIsNull(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));
    }

    private void ensurePersonaBelongsToUser(Long userId, Long personaId) {
        personaRepository.findByIdAndUserId(personaId, userId)
                .orElseThrow(() -> new PersonaNotFoundException(personaId));
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

    public static class AnniversaryNotFoundException extends RuntimeException {
        public AnniversaryNotFoundException(Long anniversaryId) {
            super("기념일을 찾을 수 없습니다. id=" + anniversaryId);
        }
    }
}
