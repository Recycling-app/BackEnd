package com.example.recycling_app.controller;

import com.example.recycling_app.dto.*;
import com.example.recycling_app.service.GoogleAuthService;
import com.example.recycling_app.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;
    private final GoogleAuthService googleAuthService;

    // ✅ 1) 일반 회원가입
    @PostMapping("/signup")
    public ResponseEntity<?> signup(
            @Validated(LocalSignUp.class) @RequestBody UserSignupRequest request
    ) {
        userService.signup(request);
        return ResponseEntity.ok("회원가입 성공");
    }

    // ✅ 2) 구글 소셜 회원가입
    @PostMapping("/google/signup")
    public ResponseEntity<?> googleSignup(
            @Valid @RequestBody GoogleSignupRequest request
    ) {
        googleAuthService.signupWithGoogle(request);
        return ResponseEntity.ok("구글 회원가입 성공");
    }
}

