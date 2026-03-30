package com.example.conversationAI.consent.controller;

import com.example.conversationAI.consent.domain.ConsentType;
import com.example.conversationAI.consent.dto.request.UpsertConsentRequest;
import com.example.conversationAI.consent.dto.response.ConsentResponse;
import com.example.conversationAI.consent.service.ConsentService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/users/{userId}/personas/{personaId}/consents")
public class ConsentController {

    private final ConsentService consentService;

    public ConsentController(ConsentService consentService) {
        this.consentService = consentService;
    }

    @PostMapping
    public ResponseEntity<ConsentResponse> agree(@PathVariable Long userId,
                                                 @PathVariable Long personaId,
                                                 @Valid @RequestBody UpsertConsentRequest request) {
        ConsentResponse response = consentService.agree(userId, personaId, request.consentType());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<List<ConsentResponse>> list(@PathVariable Long userId,
                                                      @PathVariable Long personaId) {
        return ResponseEntity.ok(consentService.list(userId, personaId));
    }

    @PatchMapping("/{consentType}/revoke")
    public ResponseEntity<ConsentResponse> revoke(@PathVariable Long userId,
                                                  @PathVariable Long personaId,
                                                  @PathVariable ConsentType consentType) {
        return ResponseEntity.ok(consentService.revoke(userId, personaId, consentType));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException e,
                                                          HttpServletRequest request) {
        List<ErrorResponse.FieldErrorItem> errors = e.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(fe -> new ErrorResponse.FieldErrorItem(
                        fe.getField(),
                        fe.getDefaultMessage() == null ? "invalid" : fe.getDefaultMessage()
                ))
                .toList();

        ErrorResponse body = ErrorResponse.of(
                HttpStatus.BAD_REQUEST.value(),
                "VALIDATION_ERROR",
                "요청 값이 올바르지 않습니다.",
                request.getRequestURI(),
                errors
        );

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }

    @ExceptionHandler(ConsentService.UserNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleUserNotFound(ConsentService.UserNotFoundException e,
                                                            HttpServletRequest request) {
        ErrorResponse body = ErrorResponse.of(
                HttpStatus.NOT_FOUND.value(),
                "USER_NOT_FOUND",
                e.getMessage(),
                request.getRequestURI()
        );
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(body);
    }

    @ExceptionHandler(ConsentService.PersonaNotFoundException.class)
    public ResponseEntity<ErrorResponse> handlePersonaNotFound(ConsentService.PersonaNotFoundException e,
                                                               HttpServletRequest request) {
        ErrorResponse body = ErrorResponse.of(
                HttpStatus.NOT_FOUND.value(),
                "PERSONA_NOT_FOUND",
                e.getMessage(),
                request.getRequestURI()
        );
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(body);
    }

    @ExceptionHandler(ConsentService.ConsentNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleConsentNotFound(ConsentService.ConsentNotFoundException e,
                                                               HttpServletRequest request) {
        ErrorResponse body = ErrorResponse.of(
                HttpStatus.NOT_FOUND.value(),
                "CONSENT_NOT_FOUND",
                e.getMessage(),
                request.getRequestURI()
        );
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(body);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponse> handleDataIntegrity(DataIntegrityViolationException e,
                                                             HttpServletRequest request) {
        String detail = (e.getMostSpecificCause() != null && e.getMostSpecificCause().getMessage() != null)
                ? e.getMostSpecificCause().getMessage()
                : "constraint violation";

        ErrorResponse body = ErrorResponse.of(
                HttpStatus.CONFLICT.value(),
                "DATA_INTEGRITY_VIOLATION",
                detail,
                request.getRequestURI()
        );
        return ResponseEntity.status(HttpStatus.CONFLICT).body(body);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleException(Exception e,
                                                         HttpServletRequest request) {
        ErrorResponse body = ErrorResponse.of(
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "INTERNAL_SERVER_ERROR",
                "서버 오류가 발생했습니다.",
                request.getRequestURI()
        );
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(body);
    }

    public record ErrorResponse(
            LocalDateTime timestamp,
            int status,
            String code,
            String message,
            String path,
            List<FieldErrorItem> errors
    ) {
        public static ErrorResponse of(int status, String code, String message, String path) {
            return new ErrorResponse(LocalDateTime.now(), status, code, message, path, List.of());
        }

        public static ErrorResponse of(int status, String code, String message, String path, List<FieldErrorItem> errors) {
            return new ErrorResponse(LocalDateTime.now(), status, code, message, path, errors);
        }

        public record FieldErrorItem(String field, String reason) {}
    }
}
