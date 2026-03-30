package com.example.conversationAI.user.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record UserSignupRequest(
        @NotBlank(message = "email은 필수입니다.")
        @Email(message = "올바른 이메일 형식이 아닙니다.")
        String email,

        @NotBlank(message = "verifyToken은 필수입니다.")
        String verifyToken,

        @NotBlank(message = "loginId는 필수입니다.")
        @Size(min = 4, max = 20, message = "아이디는 4~20자여야 합니다.")
        @Pattern(regexp = "^[a-zA-Z0-9_]+$", message = "아이디는 영문, 숫자, 언더스코어만 사용 가능합니다.")
        String loginId,

        @NotBlank(message = "password는 필수입니다.")
        @Size(min = 8, max = 72, message = "비밀번호는 8~72자여야 합니다.")
        String password,

        @NotBlank(message = "nickname은 필수입니다.")
        @Size(max = 50, message = "nickname은 50자 이하여야 합니다.")
        String nickname
) {
    public UserSignupRequest {
        email = normalizeEmail(email);
        loginId = loginId != null ? loginId.trim() : null;
        password = trimToNull(password);
        nickname = trimToNull(nickname);
    }

    private static String normalizeEmail(String v) {
        if (v == null) return null;
        String t = v.trim();
        return t.isEmpty() ? null : t.toLowerCase();
    }

    private static String trimToNull(String v) {
        if (v == null) return null;
        String t = v.trim();
        return t.isEmpty() ? null : t;
    }
}