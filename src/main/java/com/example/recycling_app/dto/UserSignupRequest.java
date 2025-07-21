package com.example.recycling_app.dto;

import jakarta.validation.constraints.*;

import lombok.Data;

@Data
public class UserSignupRequest {

    @NotBlank(message = "이메일은 필수입니다.")
    private String email;

    @NotBlank(message = "이름은 필수입니다.")
    private String name;

    @Min(value = 1, message = "나이는 1 이상이어야 합니다.")
    @Max(value = 120, message = "나이는 120 이하이어야 합니다.")
    private int age;

    @NotBlank(message = "성별은 필수입니다.")
    private String gender;

    @NotBlank(message = "지역은 필수입니다.")
    private String region;
}

