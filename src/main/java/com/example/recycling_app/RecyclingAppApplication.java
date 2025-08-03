package com.example.recycling_app;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import jakarta.annotation.PostConstruct;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

import java.io.IOException;
import java.io.InputStream;

@SpringBootApplication
@ComponentScan(basePackages = "com.example.recycling_app") // 컴포넌트 스캔 범위 명시
public class RecyclingAppApplication {

	public static void main(String[] args) {
		SpringApplication.run(RecyclingAppApplication.class, args);
		System.out.println("Spring Boot 백엔드 애플리케이션 (BackEnd-main)이 성공적으로 시작되었습니다!");
		System.out.println("DataInitializer가 Firebase Firestore에 데이터를 로드하는지 콘솔 로그를 확인하세요.");
		System.out.println("http://localhost:8080/api/waste_guide_all");
	}

	/**
	 * 애플리케이션 시작 시 Firebase Admin SDK를 초기화합니다.
	 * 서비스 계정 키 파일 'firebase-service-account.json'을 resources 폴더에서 로드합니다.
	 */
	@PostConstruct
	public void initialize() {
		try {
			// resources 폴더 내의 서비스 계정 키 파일 경로.
			// 당신의 파일명 'firebase-service-account.json'을 사용합니다.
			InputStream serviceAccount = getClass().getClassLoader().getResourceAsStream("firebase-service-account.json");

			if (serviceAccount == null) {
				throw new IOException("Firebase service account file not found in resources: firebase-service-account.json");
			}

			FirebaseOptions options = FirebaseOptions.builder()
					.setCredentials(GoogleCredentials.fromStream(serviceAccount))
					.build();

			// FirebaseApp이 이미 초기화되었는지 확인하여 중복 초기화를 방지합니다.
			if (FirebaseApp.getApps().isEmpty()) {
				FirebaseApp.initializeApp(options);
				System.out.println("Firebase Admin SDK가 성공적으로 초기화되었습니다.");
			} else {
				System.out.println("Firebase Admin SDK는 이미 초기화되어 있습니다.");
			}
		} catch (IOException e) {
			System.err.println("Firebase Admin SDK 초기화 실패: " + e.getMessage());
			e.printStackTrace();
		} catch (Exception e) {
			System.err.println("예기치 않은 오류로 Firebase Admin SDK 초기화 실패: " + e.getMessage());
			e.printStackTrace();
		}
	}
}
