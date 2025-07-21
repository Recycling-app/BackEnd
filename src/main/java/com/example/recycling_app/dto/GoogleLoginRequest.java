package com.example.recycling_app.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class GoogleLoginRequest {

    @Schema(description = "프론트에서 받은 구글 idToken (OAuth 로그인 인증용 ID 토큰)", example = "eyJhbGciOiJSUzI1NiIsIn...")
    private String idToken;
}
