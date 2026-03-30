package com.example.conversationAI.anniversary.controller;

import com.example.conversationAI.anniversary.dto.request.CreateAnniversaryRequest;
import com.example.conversationAI.anniversary.dto.request.UpdateAnniversaryEnabledRequest;
import com.example.conversationAI.anniversary.dto.request.UpdateAnniversaryRequest;
import com.example.conversationAI.anniversary.dto.response.AnniversaryResponse;
import com.example.conversationAI.anniversary.service.AnniversaryService;
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
@RequestMapping("/users/{userId}/anniversaries")
public class AnniversaryController {

    private final AnniversaryService anniversaryService;

    public AnniversaryController(AnniversaryService anniversaryService) {
        this.anniversaryService = anniversaryService;
    }

    @PostMapping
    public ResponseEntity<AnniversaryResponse> create(@PathVariable Long userId,
                                                      @Valid @RequestBody CreateAnniversaryRequest request) {
        AnniversaryResponse response = anniversaryService.create(userId, request);

        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{anniversaryId}")
                .buildAndExpand(response.id())
                .toUri();

        return ResponseEntity.created(location).body(response);
    }

    @GetMapping
    public ResponseEntity<List<AnniversaryResponse>> list(@PathVariable Long userId,
                                                          @RequestParam(name = "personaId", required = false) Long personaId) {
        return ResponseEntity.ok(anniversaryService.list(userId, personaId));
    }

    @GetMapping("/{anniversaryId}")
    public ResponseEntity<AnniversaryResponse> get(@PathVariable Long userId,
                                                   @PathVariable Long anniversaryId) {
        return ResponseEntity.ok(anniversaryService.get(userId, anniversaryId));
    }

    @PutMapping("/{anniversaryId}")
    public ResponseEntity<AnniversaryResponse> update(@PathVariable Long userId,
                                                      @PathVariable Long anniversaryId,
                                                      @Valid @RequestBody UpdateAnniversaryRequest request) {
        return ResponseEntity.ok(anniversaryService.update(userId, anniversaryId, request));
    }

    @PatchMapping("/{anniversaryId}/enabled")
    public ResponseEntity<AnniversaryResponse> updateEnabled(@PathVariable Long userId,
                                                             @PathVariable Long anniversaryId,
                                                             @Valid @RequestBody UpdateAnniversaryEnabledRequest request) {
        return ResponseEntity.ok(anniversaryService.updateEnabled(userId, anniversaryId, request));
    }

    @DeleteMapping("/{anniversaryId}")
    public ResponseEntity<Void> delete(@PathVariable Long userId,
                                       @PathVariable Long anniversaryId) {
        anniversaryService.delete(userId, anniversaryId);
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

    @ExceptionHandler(AnniversaryService.UserNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleUserNotFound(AnniversaryService.UserNotFoundException e,
                                                            HttpServletRequest request) {
        ErrorResponse body = ErrorResponse.of(
                HttpStatus.NOT_FOUND.value(),
                "USER_NOT_FOUND",
                e.getMessage(),
                request.getRequestURI()
        );
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(body);
    }

    @ExceptionHandler(AnniversaryService.PersonaNotFoundException.class)
    public ResponseEntity<ErrorResponse> handlePersonaNotFound(AnniversaryService.PersonaNotFoundException e,
                                                               HttpServletRequest request) {
        ErrorResponse body = ErrorResponse.of(
                HttpStatus.NOT_FOUND.value(),
                "PERSONA_NOT_FOUND",
                e.getMessage(),
                request.getRequestURI()
        );
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(body);
    }

    @ExceptionHandler(AnniversaryService.AnniversaryNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleAnniversaryNotFound(AnniversaryService.AnniversaryNotFoundException e,
                                                                   HttpServletRequest request) {
        ErrorResponse body = ErrorResponse.of(
                HttpStatus.NOT_FOUND.value(),
                "ANNIVERSARY_NOT_FOUND",
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
