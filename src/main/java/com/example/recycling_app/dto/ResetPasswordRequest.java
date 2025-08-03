package com.example.recycling_app.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ResetPasswordRequest {
    @NotBlank
    @Email(message = "올바른 이메일 형식이어야 합니다.")
    private String email;
}
