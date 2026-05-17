package com.example.conversationAI.user.controller;

import com.example.conversationAI.user.dto.request.UserLoginRequest;
import com.example.conversationAI.user.dto.request.UserPasswordChangeRequest;
import com.example.conversationAI.user.dto.request.UserSignupRequest;
import com.example.conversationAI.user.dto.request.FcmTokenRequest;
import com.example.conversationAI.user.dto.response.LoginResponse;
import com.example.conversationAI.user.dto.response.UserResponse;
import com.example.conversationAI.user.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.Map;

@RestController
@RequestMapping("/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/signup")
    public ResponseEntity<UserResponse> signup(@Valid @RequestBody UserSignupRequest request) {
        UserResponse response = userService.signup(request);
        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/../{id}")
                .buildAndExpand(response.id())
                .toUri();
        return ResponseEntity.created(location).body(response);
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody UserLoginRequest request) {
        return ResponseEntity.ok(userService.login(request));
    }

    @GetMapping("/me")
    public ResponseEntity<UserResponse> me(@AuthenticationPrincipal Long userId) {
        return ResponseEntity.ok(userService.getById(userId));
    }

    @GetMapping("/{id:\\d+}")
    public ResponseEntity<UserResponse> getUser(@PathVariable("id") Long id) {
        return ResponseEntity.ok(userService.getById(id));
    }

    @PatchMapping("/me/password")
    public ResponseEntity<UserResponse> changePassword(
            @AuthenticationPrincipal Long userId,
            @Valid @RequestBody UserPasswordChangeRequest request
    ) {
        return ResponseEntity.ok(userService.changePassword(userId, request));
    }

    @PatchMapping("/me/nickname")
    public ResponseEntity<UserResponse> changeNickname(
            @AuthenticationPrincipal Long userId,
            @RequestBody Map<String, String> body
    ) {
        return ResponseEntity.ok(userService.changeNickname(userId, body.get("nickname")));
    }

    @PatchMapping("/me/email")
    public ResponseEntity<UserResponse> changeEmail(
            @AuthenticationPrincipal Long userId,
            @RequestBody Map<String, String> body
    ) {
        return ResponseEntity.ok(userService.changeEmail(userId, body.get("email"), body.get("verifyToken")));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable("id") Long id) {
        userService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/block")
    public ResponseEntity<Void> block(@PathVariable("id") Long id) {
        userService.block(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/fcm-token")
    public ResponseEntity<Void> updateFcmToken(
            @AuthenticationPrincipal Long userId,
            @Valid @RequestBody FcmTokenRequest request
    ) {
        userService.updateFcmToken(userId, request.fcmToken());
        return ResponseEntity.ok().build();
    }
}