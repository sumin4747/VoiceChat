package com.example.conversationAI.common;

import com.example.conversationAI.user.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<?> handleValidation(MethodArgumentNotValidException e,
                                              HttpServletRequest request) {
        List<Map<String, String>> errors = e.getBindingResult().getFieldErrors().stream()
                .map(fe -> Map.of("field", fe.getField(), "message",
                        fe.getDefaultMessage() != null ? fe.getDefaultMessage() : "invalid"))
                .toList();

        return ResponseEntity.badRequest().body(Map.of(
                "timestamp", LocalDateTime.now().toString(),
                "status", 400,
                "code", "VALIDATION_ERROR",
                "message", "요청 값이 올바르지 않습니다.",
                "path", request.getRequestURI(),
                "errors", errors
        ));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<?> handleIllegalArgument(IllegalArgumentException e,
                                                   HttpServletRequest request) {
        String code = switch (e.getMessage()) {
            case "INVALID_OR_EXPIRED_CODE" -> "INVALID_OR_EXPIRED_CODE";
            case "EMAIL_NOT_VERIFIED" -> "EMAIL_NOT_VERIFIED";
            default -> "BAD_REQUEST";
        };

        return ResponseEntity.badRequest().body(Map.of(
                "timestamp", LocalDateTime.now().toString(),
                "status", 400,
                "code", code,
                "message", e.getMessage(),
                "path", request.getRequestURI(),
                "errors", List.of()
        ));
    }

    @ExceptionHandler(UserService.DuplicateEmailException.class)
    public ResponseEntity<?> handleDuplicateEmail(UserService.DuplicateEmailException e,
                                                  HttpServletRequest request) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of(
                "timestamp", LocalDateTime.now().toString(),
                "status", 409,
                "code", "EMAIL_ALREADY_EXISTS",
                "message", "이미 가입된 이메일입니다.",
                "path", request.getRequestURI(),
                "errors", List.of()
        ));
    }

    @ExceptionHandler(UserService.DuplicateLoginIdException.class)
    public ResponseEntity<?> handleDuplicateLoginId(UserService.DuplicateLoginIdException e,
                                                    HttpServletRequest request) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of(
                "timestamp", LocalDateTime.now().toString(),
                "status", 409,
                "code", "LOGIN_ID_ALREADY_EXISTS",
                "message", "이미 사용 중인 아이디입니다.",
                "path", request.getRequestURI(),
                "errors", List.of()
        ));
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<?> handleDataIntegrity(DataIntegrityViolationException e,
                                                 HttpServletRequest request) {
        String detail = e.getMostSpecificCause() != null
                ? e.getMostSpecificCause().getMessage() : "constraint violation";
        return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of(
                "timestamp", LocalDateTime.now().toString(),
                "status", 409,
                "code", "DATA_INTEGRITY_VIOLATION",
                "message", detail,
                "path", request.getRequestURI(),
                "errors", List.of()
        ));
    }

    @ExceptionHandler(SecurityException.class)
    public ResponseEntity<?> handleSecurity(SecurityException e, HttpServletRequest request) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of(
                "timestamp", LocalDateTime.now().toString(),
                "status", 403,
                "code", "FORBIDDEN",
                "message", e.getMessage(),
                "path", request.getRequestURI(),
                "errors", List.of()
        ));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<?> handleException(Exception e, HttpServletRequest request) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                "timestamp", LocalDateTime.now().toString(),
                "status", 500,
                "code", e.getClass().getSimpleName(),
                "message", e.getMessage() != null ? e.getMessage() : "서버 오류가 발생했습니다.",
                "path", request.getRequestURI(),
                "errors", List.of()
        ));
    }
}