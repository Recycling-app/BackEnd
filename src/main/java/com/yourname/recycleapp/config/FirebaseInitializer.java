package com.yourname.recycleapp.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import jakarta.annotation.PostConstruct;
import org.springframework.context.annotation.Configuration;

import java.io.FileInputStream;
import java.io.IOException;


// Firebase SDK 초기화를 담당하는 설정 클래스
@Configuration
public class FirebaseInitializer {

    // 빈 생성 직후 실행되어 Firebase를 초기화
    @PostConstruct
    public void init() {
        try {
            // Firebase 서비스 계정 키 JSON 파일 경로
            FileInputStream serviceAccount =
                    new FileInputStream("src/main/resources/firebase/firebase-service-account.json");

            // Firebase 옵션 구성
            FirebaseOptions options = FirebaseOptions.builder()
                    .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                    .build();

            // Firebase 앱이 초기화 되어 있지 않으면 초기화 수행
            if (FirebaseApp.getApps().isEmpty()) {
                FirebaseApp.initializeApp(options);
                System.out.println("✅ Firebase initialized");
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
