package com.example.recycling_app.service;

import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import com.google.firebase.cloud.FirestoreClient;
import org.springframework.stereotype.Service;

import java.util.concurrent.ExecutionException;


// 사용자 정보를 Firestore에서 조회하고 권한(역할)을 판별하는 서비스 클래스
@Service
public class UserService {

    private static final String COLLECTION_NAME = "users"; // 사용자 정보를 저장한 Firestore 컬렉션 이름

    // UID에 해당하는 사용자의 역할(role) 정보를 Firestore에서 조회
    public String getUserRole(String uid) throws ExecutionException, InterruptedException {
        Firestore db = FirestoreClient.getFirestore(); // Firestore 인스턴스 가져오기

        // 해당 UID의 문서를 비동기적으로 조회하고 결과를 동기적으로 대기
        DocumentSnapshot doc = db.collection(COLLECTION_NAME)
                .document(uid)
                .get()
                .get();

        // 문서가 존재하지 않으면 null 반환
        if (!doc.exists()) {
            return null;
        }

        // 문서의 'role' 필드 값을 문자열로 반환 (예: "admin", "user")
        return doc.getString("role");
    }

    // UID 사용자가 관리자(admin)인지 확인하는 메서드
    public boolean isAdmin(String uid) throws ExecutionException, InterruptedException {
        String role = getUserRole(uid);    // 사용자의 역할 가져오기
        return "admin".equals(role);       // 역할이 "admin"인지 비교 후 결과 반환
    }
}