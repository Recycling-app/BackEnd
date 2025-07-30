package com.example.recycling_app;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class RecyclingAppApplication {
	public static void main(String[] args) {
		// Spring Boot 애플리케이션을 실행합니다.
		SpringApplication.run(RecyclingAppApplication.class, args);
		System.out.println("Spring Boot 백엔드 애플리케이션 (BackEnd-main)이 성공적으로 시작되었습니다!");
		System.out.println("DataInitializer가 Firebase Firestore에 데이터를 로드하는지 콘솔 로그를 확인하세요.");
		System.out.println("http://localhost:8080/api/waste-guides?region=서울 등으로 API 테스트 가능합니다 (데이터 로드 후).");
	}
}
