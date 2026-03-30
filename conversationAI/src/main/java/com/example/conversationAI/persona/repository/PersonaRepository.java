package com.example.conversationAI.persona.repository;

import com.example.conversationAI.persona.domain.Persona;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PersonaRepository extends JpaRepository<Persona, Long> {

    List<Persona> findAllByUserId(Long userId);

    Optional<Persona> findByIdAndUserId(Long id, Long userId);

    Optional<Persona> findByIdAndUserIdAndDeletedAtIsNull(Long id, Long userId);


}
