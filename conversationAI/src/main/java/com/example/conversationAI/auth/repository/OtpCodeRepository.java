package com.example.conversationAI.auth.repository;

import com.example.conversationAI.auth.domain.OtpCode;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface OtpCodeRepository extends JpaRepository<OtpCode, Long> {

    Optional<OtpCode> findTopByEmailAndCodeOrderByCreatedAtDesc(String email, String code);

    Optional<OtpCode> findTopByEmailAndVerifyTokenOrderByCreatedAtDesc(String email, String verifyToken);
}