package com.example.conversationAI.responseStyle.controller;

import com.example.conversationAI.responseStyle.dto.request.UpsertResponseStyleRequest;
import com.example.conversationAI.responseStyle.dto.response.ResponseStyleResponse;
import com.example.conversationAI.responseStyle.service.ResponseStyleService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/users/personas/{personaId}/response-style")
public class ResponseStyleController {

    private final ResponseStyleService responseStyleService;

    public ResponseStyleController(ResponseStyleService responseStyleService) {
        this.responseStyleService = responseStyleService;
    }

    /** PUT /users/personas/{personaId}/response-style — 응답 스타일 설정/변경 */
    @PutMapping
    public ResponseEntity<ResponseStyleResponse> upsert(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long personaId,
            @Valid @RequestBody UpsertResponseStyleRequest request
    ) {
        return ResponseEntity.ok(responseStyleService.upsert(userId, personaId, request));
    }

    /** GET /users/personas/{personaId}/response-style — 현재 응답 스타일 조회 */
    @GetMapping
    public ResponseEntity<ResponseStyleResponse> get(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long personaId
    ) {
        return ResponseEntity.ok(responseStyleService.get(userId, personaId));
    }
}