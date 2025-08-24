package com.example.recycling_app.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

@Configuration
public class FirebaseInitializer {

    @Value("${firebase.sdk.path}")
    private String firebaseSdkPath;

    @Value("${firebase.storage.bucket}")
    private String firebaseStorageBucket;

    @PostConstruct
    public void init() {
        try {
            FileInputStream serviceAccount =
                    new FileInputStream("src/main/resources/firebase/firebase-service-account.json");

            FirebaseOptions options = FirebaseOptions.builder()
                    .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                    .setStorageBucket("your-name-382bf.firebasestorage.app")
                    .build();

            // Firebase 앱이 초기화 되어 있지 않으면 초기화 수행
            if (FirebaseApp.getApps().isEmpty()) {
                FirebaseApp.initializeApp(options);
                System.out.println("Firebase initialized");
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}