package com.example.recycling_app.service;

import com.example.recycling_app.dto.ProfileDTO;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.storage.BlobId;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.UserRecord;
import com.google.firebase.cloud.FirestoreClient;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;

// 사용자 프로필 관리 관련 비즈니스 로직 처리 서비스 클래스
@Service
public class ProfileService {
    private static final String COLLECTION_NAME = "users";                  // Firestore 사용자 컬렉션명
    private static final String BUCKET_NAME = "your-name-382bf.firebasestorage.app"; // Firebase Storage 버킷명 (실제 값으로 변경 필요)

    // UID로 Firestore에서 사용자 프로필 조회
    public ProfileDTO getProfile(String uid) throws ExecutionException, InterruptedException {
        Firestore db = FirestoreClient.getFirestore();
        var doc = db.collection(COLLECTION_NAME)
                .document(uid)
                .get()
                .get();

        if (!doc.exists()) {
            throw new IllegalArgumentException("해당 UID에 대한 프로필이 존재하지 않습니다.");
        }

        return doc.toObject(ProfileDTO.class); // 프로필 DTO 반환
    }

    // UID로 Firestore에 사용자 프로필 저장 또는 덮어쓰기
    public String saveProfile(String uid, ProfileDTO profileDTO) throws ExecutionException, InterruptedException {
        Firestore db = FirestoreClient.getFirestore();
        db.collection(COLLECTION_NAME)
                .document(uid)
                .set(profileDTO)  // 프로필 데이터 저장
                .get();

        return "Success";
    }

    // UID로 Firestore에서 사용자 프로필 삭제
    public String deleteProfile(String uid) throws ExecutionException, InterruptedException {
        Firestore db = FirestoreClient.getFirestore();
        db.collection(COLLECTION_NAME)
                .document(uid)
                .delete()      // 프로필 문서 삭제
                .get();

        return "Success";
    }

    // UID로 Firestore 프로필 삭제 + Firebase Authentication 계정 삭제 (회원 탈퇴)
    public String deleteUserAccount(String uid) throws ExecutionException, InterruptedException, FirebaseAuthException {
        deleteProfile(uid);                    // Firestore 프로필 삭제
        FirebaseAuth.getInstance().deleteUser(uid); // Firebase Auth 사용자 삭제
        return "회원 탈퇴 완료";
    }

    // 프로필 이미지를 Firebase Storage에 업로드 후 Firestore에 URL 저장 (옵션)
    public String uploadProfileImage(String uid, MultipartFile file, boolean saveToFirestore) throws IOException, ExecutionException, InterruptedException {
        String fileName = "profile_images/" + uid + "_" + file.getOriginalFilename();

        Storage storage = StorageOptions.getDefaultInstance().getService();
        BlobId blobId = BlobId.of(BUCKET_NAME, fileName);
        BlobInfo blobInfo = BlobInfo.newBuilder(blobId)
                .setContentType(file.getContentType())  // 업로드 파일 타입 설정
                .build();

        storage.create(blobInfo, file.getBytes());   // 파일 업로드

        String imageUrl = "https://storage.googleapis.com/" + BUCKET_NAME + "/" + fileName; // 공개 URL 생성

        if (saveToFirestore) {
            Firestore db = FirestoreClient.getFirestore();
            DocumentReference docRef = db.collection(COLLECTION_NAME).document(uid);

            Map<String, Object> updates = new HashMap<>();
            updates.put("profileImageUrl", imageUrl); // 프로필 이미지 URL 업데이트
            docRef.update(updates).get();              // Firestore에 반영
        }

        return imageUrl;  // 업로드된 이미지 URL 반환
    }

    // UID에 해당하는 사용자의 일부 프로필 필드만 수정
    public String updateProfileFields(String uid, Map<String, Object> updates) throws ExecutionException, InterruptedException {
        Firestore db = FirestoreClient.getFirestore();
        db.collection(COLLECTION_NAME)
                .document(uid)
                .update(updates)  // 일부 필드만 업데이트
                .get();

        return "프로필 일부 항목 수정 완료";
    }

    // 사용자 비밀번호 변경
    // 이 메서드는 Firebase Authentication을 사용하여 비밀번호를 직접 업데이트합니다.
    // 현재 비밀번호 검증은 클라이언트 측에서 이루어지거나, Firebase Admin SDK에서 직접적으로
    // '현재 비밀번호'를 검증하는 API가 없으므로, 이 메서드는 단순히 새 비밀번호로 업데이트하는 역할을 합니다.
    // 보안 강화를 위해 클라이언트 측에서 Firebase Authentication의 reauthenticateWithCredential()을 먼저 호출하는 것을 권장합니다.
    public String changePassword(String uid, String currentPassword, String newPassword) throws FirebaseAuthException {
        FirebaseAuth auth = FirebaseAuth.getInstance();

        // Firebase Admin SDK는 사용자 계정의 현재 비밀번호를 직접 검증하는 API를 제공하지 않습니다.
        // 따라서 `currentPassword`는 클라이언트 측 유효성 검사 또는 재인증을 위한 정보로 사용됩니다.
        // 여기서는 `newPassword`로 사용자 비밀번호를 업데이트하는 로직만 포함합니다.
        try {
            UserRecord.UpdateRequest request = new UserRecord.UpdateRequest(uid)
                    .setPassword(newPassword); // 새 비밀번호로 업데이트
            auth.updateUser(request);
            return "비밀번호 변경 완료";
        } catch (FirebaseAuthException e) {
            // FirebaseAuthException을 다시 던져서 원래 오류를 전파합니다.
            throw e;
        }
    }
}