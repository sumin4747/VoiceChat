package com.example.conversationAI.personaDescription.controller;

import com.example.conversationAI.personaDescription.dto.request.UpsertPersonaDescriptionRequest;
import com.example.conversationAI.personaDescription.dto.response.PersonaDescriptionResponse;
import com.example.conversationAI.personaDescription.service.PersonaDescriptionService;
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
@RequestMapping("/users/{userId}/personas/{personaId}/description")
public class PersonaDescriptionController {

    private final PersonaDescriptionService personaDescriptionService;

    public PersonaDescriptionController(PersonaDescriptionService personaDescriptionService) {
        this.personaDescriptionService = personaDescriptionService;
    }

    @PutMapping
    public ResponseEntity<PersonaDescriptionResponse> upsert(
            @PathVariable Long userId,
            @PathVariable Long personaId,
            @Valid @RequestBody UpsertPersonaDescriptionRequest request) {

        return ResponseEntity.ok(
                personaDescriptionService.upsert(userId, personaId, request)
        );
    }

    @GetMapping
    public ResponseEntity<PersonaDescriptionResponse> get(
            @PathVariable Long userId,
            @PathVariable Long personaId) {

        return ResponseEntity.ok(
                personaDescriptionService.get(userId, personaId)
        );
    }

    @DeleteMapping
    public ResponseEntity<Void> delete(
            @PathVariable Long userId,
            @PathVariable Long personaId) {

        personaDescriptionService.delete(userId, personaId);
        return ResponseEntity.noContent().build();
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

    @ExceptionHandler(PersonaDescriptionService.PersonaNotFoundException.class)
    public ResponseEntity<ErrorResponse> handlePersonaNotFound(PersonaDescriptionService.PersonaNotFoundException e,
                                                               HttpServletRequest request) {
        ErrorResponse body = ErrorResponse.of(
                HttpStatus.NOT_FOUND.value(),
                "PERSONA_NOT_FOUND",
                e.getMessage(),
                request.getRequestURI()
        );
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(body);
    }

    @ExceptionHandler(PersonaDescriptionService.PersonaDescriptionNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleDescriptionNotFound(PersonaDescriptionService.PersonaDescriptionNotFoundException e,
                                                                   HttpServletRequest request) {
        ErrorResponse body = ErrorResponse.of(
                HttpStatus.NOT_FOUND.value(),
                "PERSONA_DESCRIPTION_NOT_FOUND",
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
