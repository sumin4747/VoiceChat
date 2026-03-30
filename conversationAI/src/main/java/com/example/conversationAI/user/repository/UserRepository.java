package com.example.conversationAI.user.repository;

import com.example.conversationAI.user.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmailAndDeletedAtIsNull(String email);

    Optional<User> findByIdAndDeletedAtIsNull(Long id);

    Optional<User> findByLoginIdAndDeletedAtIsNull(String loginId);

    boolean existsByEmailAndDeletedAtIsNull(String email);

    boolean existsByLoginId(String loginId);
}