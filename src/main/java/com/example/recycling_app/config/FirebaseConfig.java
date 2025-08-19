package com.example.recycling_app.config;

import com.google.cloud.storage.Bucket;
import com.google.firebase.cloud.StorageClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FirebaseConfig {

    // Firebase Storage 버킷을 Bean으로 등록
    @Bean
    public Bucket bucket() {
        // FirebaseInitializer에서 초기화된 앱을 기반으로 Storage 클라이언트를 가져옵니다.
        // "your-name-382bf.firebasestorage.app"는 FirebaseInitializer에 설정된 버킷 이름입니다.
        return StorageClient.getInstance().bucket();
    }
}
