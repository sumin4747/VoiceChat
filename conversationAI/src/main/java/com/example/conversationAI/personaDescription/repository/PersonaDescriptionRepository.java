package com.example.conversationAI.personaDescription.repository;

import com.example.conversationAI.personaDescription.domain.PersonaDescription;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PersonaDescriptionRepository extends JpaRepository<PersonaDescription, Long> {
    Optional<PersonaDescription> findByPersonaId(Long personaId);
    boolean existsByPersonaId(Long personaId);
}
