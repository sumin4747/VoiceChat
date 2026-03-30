package com.example.conversationAI.auth.service;

import com.example.conversationAI.auth.domain.OtpCode;
import com.example.conversationAI.auth.repository.OtpCodeRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

@Service
@Transactional
public class OtpCodeService {

    private final OtpCodeRepository otpRepository;

    public OtpCodeService(OtpCodeRepository otpRepository) {
        this.otpRepository = otpRepository;
    }

    public int sendOtp(String email) {
        String code = generateCode();
        OtpCode otp = OtpCode.create(email, code, 300);
        otpRepository.save(otp);
        System.out.println("DEV OTP CODE for " + email + " : " + code);
        return 300;
    }

    /**
     * OTP 인증 후 verifyToken 발급.
     * verifyToken은 회원가입 시 이메일 인증 완료 여부 확인에 사용됨.
     * OTP 코드에 verifyToken을 저장해두고, 회원가입 시 검증.
     */
    public String verify(String email, String code) {
        OtpCode otp = otpRepository
                .findTopByEmailAndCodeOrderByCreatedAtDesc(email, code)
                .orElseThrow(() -> new IllegalArgumentException("INVALID_OR_EXPIRED_CODE"));

        if (otp.isExpired() || otp.isVerified()) {
            throw new IllegalArgumentException("INVALID_OR_EXPIRED_CODE");
        }

        // verifyToken 생성 후 OTP에 저장
        String verifyToken = UUID.randomUUID().toString();
        otp.markVerified(verifyToken);

        return verifyToken;
    }

    /**
     * 회원가입 시 verifyToken 유효성 검증.
     * 해당 email의 최근 인증된 OTP에서 verifyToken이 일치하는지 확인.
     */
    public void validateVerifyToken(String email, String verifyToken) {
        OtpCode otp = otpRepository
                .findTopByEmailAndVerifyTokenOrderByCreatedAtDesc(email, verifyToken)
                .orElseThrow(() -> new IllegalArgumentException("EMAIL_NOT_VERIFIED"));

        if (!otp.isVerified()) {
            throw new IllegalArgumentException("EMAIL_NOT_VERIFIED");
        }
    }

    private String generateCode() {
        int number = ThreadLocalRandom.current().nextInt(100000, 999999);
        return String.valueOf(number);
    }
}