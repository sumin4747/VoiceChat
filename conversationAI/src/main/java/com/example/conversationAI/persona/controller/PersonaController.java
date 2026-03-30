package com.example.conversationAI.persona.controller;

import com.example.conversationAI.persona.dto.request.CreatePersonaRequest;
import com.example.conversationAI.persona.dto.response.PersonaResponse;
import com.example.conversationAI.persona.service.PersonaService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/users/{userId}/personas")
public class PersonaController {

    private final PersonaService personaService;

    public PersonaController(PersonaService personaService) {
        this.personaService = personaService;
    }

    @PostMapping
    public ResponseEntity<PersonaResponse> create(@PathVariable Long userId,
                                                  @Valid @RequestBody CreatePersonaRequest request) {

        PersonaResponse response = personaService.create(userId, request);

        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{personaId}")
                .buildAndExpand(response.id())
                .toUri();

        return ResponseEntity.created(location).body(response);
    }

    @GetMapping
    public ResponseEntity<List<PersonaResponse>> list(@PathVariable Long userId) {
        return ResponseEntity.ok(personaService.list(userId));
    }

    @GetMapping("/{personaId}")
    public ResponseEntity<PersonaResponse> get(@PathVariable Long userId,
                                               @PathVariable Long personaId) {
        return ResponseEntity.ok(personaService.get(userId, personaId));
    }

    @DeleteMapping("/{personaId}")
    public ResponseEntity<Void> delete(@PathVariable Long userId,
                                       @PathVariable Long personaId) {
        personaService.delete(userId, personaId);
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

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgument(IllegalArgumentException e,
                                                               HttpServletRequest request) {
        ErrorResponse body = ErrorResponse.of(
                HttpStatus.BAD_REQUEST.value(),
                "BAD_REQUEST",
                e.getMessage(),
                request.getRequestURI()
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }

    @ExceptionHandler(PersonaService.UserNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleUserNotFound(PersonaService.UserNotFoundException e,
                                                            HttpServletRequest request) {
        ErrorResponse body = ErrorResponse.of(
                HttpStatus.NOT_FOUND.value(),
                "USER_NOT_FOUND",
                e.getMessage(),
                request.getRequestURI()
        );
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(body);
    }

    @ExceptionHandler(PersonaService.PersonaNotFoundException.class)
    public ResponseEntity<ErrorResponse> handlePersonaNotFound(PersonaService.PersonaNotFoundException e,
                                                               HttpServletRequest request) {
        ErrorResponse body = ErrorResponse.of(
                HttpStatus.NOT_FOUND.value(),
                "PERSONA_NOT_FOUND",
                e.getMessage(),
                request.getRequestURI()
        );
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(body);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponse> handleDataIntegrity(DataIntegrityViolationException e,
                                                             HttpServletRequest request) {
        ErrorResponse body = ErrorResponse.of(
                HttpStatus.CONFLICT.value(),
                "DATA_INTEGRITY_VIOLATION",
                "중복이거나 제약조건을 위반했습니다.",
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
