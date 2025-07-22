package com.example.recycling_app.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User {
    private String uid;        // Firebase UID or Google 계정 ID
    private String provider;   // 로그인 방식 google / local
    private String email;      // 로그인 아이디로 사용
    private String name;
    private String password;
    private int age;
    private String gender;
    private String region;
    private String phoneNumber;
}