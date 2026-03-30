package com.example.conversationAI.auth.domain;

import java.time.LocalDateTime;
import jakarta.persistence.*;

@Entity
@Table(name = "otp_codes")
public class OtpCode {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String email;

    private String code;

    @Column(name = "verify_token")
    private String verifyToken;

    private LocalDateTime expiresAt;

    private boolean verified;

    @Column(insertable = false, updatable = false)
    private LocalDateTime createdAt;

    protected OtpCode() {}

    public static OtpCode create(String email, String code, int validSeconds) {
        OtpCode otp = new OtpCode();
        otp.email = email;
        otp.code = code;
        otp.expiresAt = LocalDateTime.now().plusSeconds(validSeconds);
        otp.verified = false;
        return otp;
    }

    public boolean isExpired() {
        return LocalDateTime.now().isAfter(this.expiresAt);
    }

    public void markVerified(String verifyToken) {
        this.verified = true;
        this.verifyToken = verifyToken;
    }

    public String getEmail() { return email; }
    public String getCode() { return code; }
    public String getVerifyToken() { return verifyToken; }
    public boolean isVerified() { return verified; }
}