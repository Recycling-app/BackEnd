package com.example.recycling_app.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class LogoutRequest {
    @NotBlank(message = "액세스 토큰은 필수입니다.")
    private String accessToken;

    private String refreshToken;
}
