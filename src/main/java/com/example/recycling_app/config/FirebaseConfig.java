package com.example.recycling_app.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.firestore.Firestore;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.cloud.FirestoreClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;

import jakarta.annotation.PostConstruct; // Spring Boot 3.x부터는 'javax' 대신 'jakarta' 사용
import java.io.IOException;

import jakarta.annotation.PostConstruct;
import org.springframework.core.io.ClassPathResource;

import java.io.InputStream;

@Configuration // 이 클래스가 스프링 설정 클래스임을 나타냅니다.
public class FirebaseConfig {

    // application.properties에서 설정한 Firebase 서비스 계정 키 파일의 리소스 경로를 주입받습니다.
    @Value("${firebase.config.path}")
    private Resource firebaseConfigResource;

    @PostConstruct // 스프링 애플리케이션 초기화 시 이 메서드를 실행합니다.
    public void initializeFirebase() {
        try {
            // FirebaseApp이 이미 초기화되었는지 확인하여 중복 초기화를 방지합니다.
            // (멀티모듈 환경이나 테스트 환경에서 발생할 수 있는 문제 방지)
            if (FirebaseApp.getApps().isEmpty()) {
                FirebaseOptions options = FirebaseOptions.builder()
                        .setCredentials(GoogleCredentials.fromStream(firebaseConfigResource.getInputStream()))
                        // Realtime Database를 사용한다면 .setDatabaseUrl("https://YOUR_PROJECT_ID.firebaseio.com") 추가 가능
                        .build();
                FirebaseApp.initializeApp(options);
                System.out.println("Firebase Admin SDK가 성공적으로 초기화되었습니다.");
            }
        } catch (IOException e) {
            System.err.println("Firebase Admin SDK 초기화 중 오류 발생: " + e.getMessage());
            e.printStackTrace();
            // Firebase 초기화 실패는 애플리케이션 시작에 치명적이므로 런타임 예외를 발생시킵니다.
            throw new RuntimeException("Firebase Admin SDK 초기화에 실패했습니다.", e);
        }
    }

    // Firestore 인스턴스를 Spring 빈으로 등록합니다.
    // 이렇게 하면 다른 서비스나 컨트롤러에서 @Autowired를 통해 Firestore 객체를 주입받아 사용할 수 있습니다.
    @Bean
    public Firestore firestore() {
        return FirestoreClient.getFirestore();
      
    @PostConstruct
    public void initFirebase() {
        try {
            InputStream serviceAccount = new ClassPathResource("firebase/serviceAccountKey.json").getInputStream();

            FirebaseOptions options = FirebaseOptions.builder()
                    .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                    .build();

            if (FirebaseApp.getApps().isEmpty()) {
                FirebaseApp.initializeApp(options);
                System.out.println("Firebase Admin SDK 초기화 완료");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Bean
    public Firestore firestore() {
        return FirestoreClient.getFirestore(); // FirebaseApp이 먼저 초기화되어 있어야 안전

    }
}
