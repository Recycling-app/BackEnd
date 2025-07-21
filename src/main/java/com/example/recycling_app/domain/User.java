package com.example.recycling_app.domain;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class User {
    private String uid;        // Firebase UID or Google 계정 ID
    private String email;
    private String name;
    private int age;
    private String gender;
    private String region;
    private String provider;   // 로그인 방식 google / local
}