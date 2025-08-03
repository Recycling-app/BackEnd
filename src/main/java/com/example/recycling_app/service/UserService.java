package com.example.recycling_app.service;

import com.example.recycling_app.domain.User;
import com.example.recycling_app.dto.UserSignupRequest;
import com.example.recycling_app.repository.UserRepository;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import com.google.firebase.cloud.FirestoreClient;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.concurrent.ExecutionException;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private static final String COLLECTION_NAME = "users"; // 사용자 정보를 저장한 Firestore 컬렉션 이름

    public void signup(UserSignupRequest req) {
        try {
            if (userRepository.findByEmail(req.getEmail()).isPresent()) {
                throw new IllegalStateException("이미 존재하는 이메일입니다.");
            }

            String uid = "local-" + req.getEmail();
            String hashed = passwordEncoder.encode(req.getPassword());

            User user = User.builder()
                    .uid(uid)
                    .provider("local")
                    .email(req.getEmail())
                    .password(hashed)
                    .name(req.getName())
                    .phoneNumber(req.getPhoneNumber())
                    .age(req.getAge())
                    .gender(req.getGender())
                    .region(req.getRegion())
                    .build();

            userRepository.save(user);

        } catch (Exception e) {
            throw new RuntimeException("회원가입 실패: " + e.getMessage());
        }
    }

    // 이메일 찾기 (이름 + 전화번호 기반)
    public String findEmail(String name, String phoneNumber) {
        try {
            return userRepository.findByNameAndPhoneNumber(name, phoneNumber)
                    .map(User::getEmail)
                    .orElseThrow(() -> new IllegalStateException("일치하는 사용자를 찾을 수 없습니다."));
        } catch (Exception e) {
            throw new RuntimeException("이메일 찾기 중 오류 발생: " + e.getMessage());
        }
    }
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

