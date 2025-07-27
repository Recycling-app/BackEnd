package com.example.recycling_app.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class GoogleLoginRequest {
    @NotBlank
    private String idToken;
}