package com.example.recycling_app.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class GoogleSignupRequest {

    @NotBlank
    private String idToken;

    @NotBlank
    private String name;

    @NotBlank
    @Size(min = 8)
    private String password;

    @NotBlank
    @Pattern(regexp = "^(010|011|016|017|018|019)-?[0-9]{3,4}-?[0-9]{4}$")
    private String phoneNumber;

    @Min(1)
    @Max(120)
    private int age;

    @NotBlank
    private String gender;

    @NotBlank
    private String region;
}
