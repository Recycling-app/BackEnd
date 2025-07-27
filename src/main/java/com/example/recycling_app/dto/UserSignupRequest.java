package com.example.recycling_app.dto;

import jakarta.validation.constraints.*;

import lombok.Data;

@Data
public class UserSignupRequest {

    @NotBlank(groups = LocalSignUp.class)
    @Email(groups = LocalSignUp.class)
    private String email;

    @NotBlank(groups = LocalSignUp.class)
    private String name;

    @NotBlank(groups = LocalSignUp.class)
    @Size(min = 8, message = "비밀번호는 8자 이상이어야 합니다.", groups = LocalSignUp.class)
    private String password;

    @NotBlank(groups = LocalSignUp.class)
    @Pattern(regexp = "^(010|011|016|017|018|019)-?[0-9]{3,4}-?[0-9]{4}$", message = "올바른 전화번호를 입력하세요")
    private String phoneNumber;

    @Min(value = 1, groups = LocalSignUp.class)
    @Max(value = 120, groups = LocalSignUp.class)
    private int age;

    @NotBlank(groups = LocalSignUp.class)
    private String gender;

    @NotBlank(groups = LocalSignUp.class)
    private String region;
}
