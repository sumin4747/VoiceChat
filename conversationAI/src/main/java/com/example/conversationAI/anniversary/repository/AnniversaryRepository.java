package com.example.conversationAI.anniversary.repository;

import com.example.conversationAI.anniversary.domain.Anniversary;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDate;

public interface AnniversaryRepository extends JpaRepository<Anniversary, Long> {

    List<Anniversary> findAllByUserIdOrderByEventDateAsc(Long userId);

    List<Anniversary> findAllByUserIdAndPersonaIdOrderByEventDateAsc(Long userId, Long personaId);

    Optional<Anniversary> findByIdAndUserId(Long id, Long userId);

    @Query("SELECT a FROM Anniversary a WHERE a.enabled = true " +
            "AND ((a.repeatYearly = true " +
            "AND FUNCTION('MONTH', a.eventDate) = FUNCTION('MONTH', :today) " +
            "AND FUNCTION('DAY', a.eventDate) = FUNCTION('DAY', :today)) " +
            "OR (a.repeatYearly = false AND a.eventDate = :today))")
    List<Anniversary> findTodayAnniversaries(@Param("today") LocalDate today);
}
