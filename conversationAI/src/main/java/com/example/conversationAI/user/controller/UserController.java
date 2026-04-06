package com.example.conversationAI.user.controller;

import com.example.conversationAI.user.dto.request.UserLoginRequest;
import com.example.conversationAI.user.dto.request.UserPasswordChangeRequest;
import com.example.conversationAI.user.dto.request.UserSignupRequest;
import com.example.conversationAI.user.dto.response.LoginResponse;
import com.example.conversationAI.user.dto.response.UserResponse;
import com.example.conversationAI.user.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;

@RestController
@RequestMapping("/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    /** POST /users/signup — 회원가입 */
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

    /** POST /users/login — 로그인 */
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody UserLoginRequest request) {
        LoginResponse response = userService.login(request);
        return ResponseEntity.ok(response);
    }

    /** GET /users/me — 내 정보 조회 */
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

    @PatchMapping("/me/fcm-token")
    public ResponseEntity<Void> updateFcmToken(
            @AuthenticationPrincipal Long userId,
            @RequestBody Map<String, String> request
    ) {
        userService.updateFcmToken(userId, request.get("fcmToken"));
        return ResponseEntity.noContent().build();
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
}