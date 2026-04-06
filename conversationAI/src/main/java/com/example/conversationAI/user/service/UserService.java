package com.example.conversationAI.user.service;

import com.example.conversationAI.auth.service.OtpCodeService;
import com.example.conversationAI.user.domain.User;
import com.example.conversationAI.user.dto.request.UserLoginRequest;
import com.example.conversationAI.user.dto.request.UserPasswordChangeRequest;
import com.example.conversationAI.user.dto.request.UserSignupRequest;
import com.example.conversationAI.user.dto.response.LoginResponse;
import com.example.conversationAI.user.dto.response.UserResponse;
import com.example.conversationAI.user.repository.UserRepository;
import com.example.conversationAI.security.jwt.JwtProvider;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class UserService {

    private final UserRepository userRepository;
    private final OtpCodeService otpCodeService;
    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    private final JwtProvider jwtProvider;

    @PersistenceContext
    private EntityManager entityManager;

    public UserService(UserRepository userRepository,
                       OtpCodeService otpCodeService,
                       JwtProvider jwtProvider) {
        this.userRepository = userRepository;
        this.otpCodeService = otpCodeService;
        this.jwtProvider = jwtProvider;
    }

    /**
     * 회원가입.
     * verifyToken으로 이메일 인증 완료 여부 확인 후 계정 생성.
     */
    public UserResponse signup(UserSignupRequest request) {
        // 1. verifyToken 유효성 검증
        otpCodeService.validateVerifyToken(request.email(), request.verifyToken());

        // 2. 이메일 중복 확인
        if (userRepository.existsByEmailAndDeletedAtIsNull(request.email())) {
            throw new DuplicateEmailException(request.email());
        }

        // 3. loginId 중복 확인
        if (userRepository.existsByLoginId(request.loginId())) {
            throw new DuplicateLoginIdException(request.loginId());
        }

        // 4. 계정 생성
        String hashed = passwordEncoder.encode(request.password());
        User user = User.create(request.email(), request.loginId(), hashed, request.nickname());
        User saved = userRepository.save(user);

        return UserResponse.from(saved);
    }

    /**
     * loginId/password 로그인.
     * 응답: { token, isNewUser: false, user }
     */
    @Transactional(readOnly = true)
    public LoginResponse login(UserLoginRequest request) {
        User user = userRepository.findByLoginIdAndDeletedAtIsNull(request.loginId())
                .orElseThrow(() -> new IllegalArgumentException("아이디 또는 비밀번호가 올바르지 않습니다."));

        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new IllegalArgumentException("아이디 또는 비밀번호가 올바르지 않습니다.");
        }

        String token = jwtProvider.generateToken(user.getId());
        return new LoginResponse(token, false, new LoginResponse.UserInfo(user.getId(), user.getNickname()));
    }

    @Transactional(readOnly = true)
    public UserResponse getById(Long userId) {
        User user = userRepository.findByIdAndDeletedAtIsNull(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));
        return UserResponse.from(user);
    }

    public UserResponse changePassword(Long userId, UserPasswordChangeRequest request) {
        User user = userRepository.findByIdAndDeletedAtIsNull(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));

        if (!passwordEncoder.matches(request.currentPassword(), user.getPasswordHash())) {
            throw new IllegalArgumentException("현재 비밀번호가 올바르지 않습니다.");
        }

        user.changePasswordHash(passwordEncoder.encode(request.newPassword()));
        return UserResponse.from(user);
    }

    public void updateFcmToken(Long userId, String fcmToken) {
        User user = userRepository.findByIdAndDeletedAtIsNull(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));
        user.updateFcmToken(fcmToken);
    }

    public void delete(Long userId) {
        User user = userRepository.findByIdAndDeletedAtIsNull(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));
        user.delete();
    }

    public void block(Long userId) {
        User user = userRepository.findByIdAndDeletedAtIsNull(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));
        user.block();
    }

    // ── 예외 클래스 ──────────────────────────────────────────────────────

    public static class UserNotFoundException extends RuntimeException {
        public UserNotFoundException(Long userId) {
            super("사용자를 찾을 수 없습니다. id=" + userId);
        }
    }

    public static class DuplicateEmailException extends RuntimeException {
        public DuplicateEmailException(String email) {
            super("이미 가입된 이메일입니다. email=" + email);
        }
    }

    public static class DuplicateLoginIdException extends RuntimeException {
        public DuplicateLoginIdException(String loginId) {
            super("이미 사용 중인 아이디입니다. loginId=" + loginId);
        }
    }
}