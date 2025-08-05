package com.example.recycling_app.controller;

import com.example.recycling_app.dto.*;
import com.example.recycling_app.service.AuthService;
import com.example.recycling_app.service.LogoutService;
import com.example.recycling_app.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "인증", description = "회원가입, 로그인, 로그아웃 API")
public class AuthController {

    private final UserService userService;
    private final AuthService authService;
    private final LogoutService logoutService; // 추가

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

    @PostMapping("/logout")
    @Operation(
            summary = "로그아웃",
            description = "사용자를 로그아웃시키고 토큰을 무효화합니다.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    public ResponseEntity<String> logout(@Valid @RequestBody LogoutRequest request) {
        logoutService.logout(request.getAccessToken(), request.getRefreshToken());
        return ResponseEntity.ok("로그아웃이 완료되었습니다.");
    }

    @PostMapping("/find-email")
    @Operation(summary = "아이디(이메일) 찾기", description = "이름과 전화번호로 등록된 이메일을 반환합니다.")
    public ResponseEntity<Map<String, String>> findEmail(@Valid @RequestBody FindEmailRequest request) {
        String foundEmail = userService.findEmail(request.getName(), request.getPhoneNumber());

        Map<String, String> response = new HashMap<>();
        response.put("email", foundEmail);

        return ResponseEntity.ok(response);
    }

    @PostMapping("/reset-password")
    @Operation(summary = "임시 비밀번호 이메일로 발송", description = "비밀번호를 재설정하여 이메일로 임시 비밀번호를 보내줍니다.")
    public ResponseEntity<String> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        authService.resetPassword(request.getEmail());
        return ResponseEntity.ok("임시 비밀번호가 이메일로 발송되었습니다.");
    }
}