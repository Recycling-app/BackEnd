package com.example.recycling_app.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class JwtLoginResponse {
    @Schema(description = "JWT Access Token(앱 인증용 인가 토큰)", example = "eyJhbGciOiJIUzI1NiIsInR5cCI6...")
    private String accessToken;

    @Schema(description = "JWT Refresh Token(선택적)", example = "eyJhbGciOiJIUzI1NiIs...")
    private String refreshToken;

    @Schema(description = "토큰 타입(고정값: 'Bearer')", defaultValue = "Bearer")
    private String tokenType;

    @Schema(description = "만료시간(초 단위)", example = "3600")
    private long expiresIn;
}
