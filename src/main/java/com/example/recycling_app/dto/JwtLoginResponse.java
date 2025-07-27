package com.example.recycling_app.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class JwtLoginResponse {
    private String accessToken;
    private String refreshToken;
    private String tokenType;      // 예: "Bearer"
    private long expiresIn;        // 예: 3600 (단위: 초)
}

