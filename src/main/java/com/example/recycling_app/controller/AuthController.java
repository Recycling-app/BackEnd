package com.example.recycling_app.controller;

import com.example.recycling_app.dto.UserSignupRequest;
import com.example.recycling_app.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;

    @PostMapping("/signup")
    @Operation(
            summary = "회원가입",
            description = "구글 OAuth 인증 후 추가 정보를 받아 회원가입 처리"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "회원가입 성공"),
            @ApiResponse(responseCode = "400", description = "입력값 오류 (유효성 검증 실패)")
    })
    public ResponseEntity<?> signup(
            @RequestHeader("uid") String uid,
            @RequestHeader("provider") String provider,
            @Valid @RequestBody UserSignupRequest request
    ) {
        userService.signup(uid, provider, request);
        return ResponseEntity.ok("회원가입 성공");
    }
}
