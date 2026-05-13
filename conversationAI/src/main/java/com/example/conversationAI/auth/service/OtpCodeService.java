package com.example.conversationAI.auth.service;

import com.example.conversationAI.auth.domain.OtpCode;
import com.example.conversationAI.auth.repository.OtpCodeRepository;
import jakarta.transaction.Transactional;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

@Service
@Transactional
public class OtpCodeService {

    private final OtpCodeRepository otpRepository;
    private final JavaMailSender mailSender;

    public OtpCodeService(OtpCodeRepository otpRepository, JavaMailSender mailSender) {
        this.otpRepository = otpRepository;
        this.mailSender = mailSender;
    }

    public int sendOtp(String email) {
        String code = generateCode();
        OtpCode otp = OtpCode.create(email, code, 300);
        otpRepository.save(otp);

        sendEmail(email, code);
        System.out.println("DEV OTP CODE for " + email + " : " + code);

        return 300;
    }

    public String verify(String email, String code) {
        OtpCode otp = otpRepository
                .findTopByEmailAndCodeOrderByCreatedAtDesc(email, code)
                .orElseThrow(() -> new IllegalArgumentException("INVALID_OR_EXPIRED_CODE"));

        if (otp.isExpired() || otp.isVerified()) {
            throw new IllegalArgumentException("INVALID_OR_EXPIRED_CODE");
        }

        String verifyToken = UUID.randomUUID().toString();
        otp.markVerified(verifyToken);

        return verifyToken;
    }

    public void validateVerifyToken(String email, String verifyToken) {
        OtpCode otp = otpRepository
                .findTopByEmailAndVerifyTokenOrderByCreatedAtDesc(email, verifyToken)
                .orElseThrow(() -> new IllegalArgumentException("EMAIL_NOT_VERIFIED"));

        if (!otp.isVerified()) {
            throw new IllegalArgumentException("EMAIL_NOT_VERIFIED");
        }
    }

    private void sendEmail(String to, String code) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(to);
            message.setSubject("[VoiceChat] 이메일 인증 코드");
            message.setText(
                    "안녕하세요.\n\n" +
                            "인증 코드: " + code + "\n\n" +
                            "인증 코드는 5분간 유효합니다.\n" +
                            "본인이 요청하지 않은 경우 이 메일을 무시해주세요."
            );
            mailSender.send(message);
        } catch (Exception e) {
            System.err.println("이메일 발송 실패: " + e.getMessage());
        }
    }

    private String generateCode() {
        int number = ThreadLocalRandom.current().nextInt(100000, 999999);
        return String.valueOf(number);
    }
}