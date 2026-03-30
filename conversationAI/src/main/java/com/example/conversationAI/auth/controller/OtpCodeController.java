package com.example.conversationAI.auth.controller;

import com.example.conversationAI.auth.dto.request.SendOtpRequest;
import com.example.conversationAI.auth.dto.request.VerifyOtpRequest;
import com.example.conversationAI.auth.dto.response.VerifyOtpResponse;
import com.example.conversationAI.auth.service.OtpCodeService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/users/auth/otp")
public class OtpCodeController {

    private final OtpCodeService otpService;

    public OtpCodeController(OtpCodeService otpService) {
        this.otpService = otpService;
    }

    @PostMapping("/send")
    public ResponseEntity<Map<String, Object>> send(@RequestBody SendOtpRequest request) {
        int expires = otpService.sendOtp(request.email());
        return ResponseEntity.ok(Map.of(
                "ok", true,
                "expiresInSec", expires
        ));
    }

    /**
     * OTP 인증 후 verifyToken 반환.
     * 토큰 발급 없음 - 회원가입 시 verifyToken 사용.
     */
    @PostMapping("/verify")
    public ResponseEntity<VerifyOtpResponse> verify(@RequestBody VerifyOtpRequest request) {
        String verifyToken = otpService.verify(request.email(), request.code());
        return ResponseEntity.ok(new VerifyOtpResponse(true, true, verifyToken));
    }
}
