package com.example.recycling_app.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class FindEmailRequest {
    @NotBlank(message = "이름은 필수입니다.")
    private String name;

    @NotBlank(message = "전화번호는 필수입니다.")
    @Pattern(regexp = "^(010|011|016|017|018|019)-?[0-9]{3,4}-?[0-9]{4}$", message = "전화번호 형식이 올바르지 않습니다.")
    private String phoneNumber;
}
