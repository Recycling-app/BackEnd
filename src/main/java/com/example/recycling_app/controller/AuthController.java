package com.example.recycling_app.controller;

import com.example.recycling_app.dto.*;
import com.example.recycling_app.service.AuthService;
import com.example.recycling_app.service.GoogleAuthService;
import com.example.recycling_app.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;
    private final AuthService authService;
    private final GoogleAuthService googleAuthService;

    @PostMapping("/signup")
    public ResponseEntity<String> signup(@Valid @RequestBody UserSignupRequest request) {
        userService.signup(request);
        return ResponseEntity.ok("회원가입 성공");
    }

    @PostMapping("/login")
    public ResponseEntity<JwtLoginResponse> login(@Valid @RequestBody LoginRequest request) {
        JwtLoginResponse response = authService.login(request.getEmail(), request.getPassword());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/google/signup")
    public ResponseEntity<String> googleSignup(@Valid @RequestBody GoogleSignupRequest request) {
        googleAuthService.signupWithGoogle(request);
        return ResponseEntity.ok("구글 회원가입 성공");
    }

    @PostMapping("/google/login")
    public ResponseEntity<JwtLoginResponse> googleLogin(@Valid @RequestBody GoogleLoginRequest request) {
        JwtLoginResponse response = googleAuthService.loginWithGoogle(request.getIdToken());
        return ResponseEntity.ok(response);
    }
}
