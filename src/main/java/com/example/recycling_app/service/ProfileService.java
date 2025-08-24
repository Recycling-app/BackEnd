package com.example.recycling_app.service;

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
import com.example.recycling_app.dto.ProfileDTO;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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

        // Realtime Database 데이터 삭제 로직
        DatabaseReference userNode = FirebaseDatabase.getInstance().getReference("users").child(uid);
        userNode.removeValueAsync().get();

        deleteProfile(uid); // Firestore 프로필 삭제
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
        DocumentReference docRef = db.collection(COLLECTION_NAME).document(uid);

        // 기존 데이터 가져오기 (만약 isProfilePublic이 누락된 경우를 대비)
        ProfileDTO existingProfile = docRef.get().get().toObject(ProfileDTO.class);
        if (!updates.containsKey("isProfilePublic")) {
            updates.put("isProfilePublic", existingProfile.isProfilePublic());
        }

        docRef.update(updates).get();

        return "프로필 일부 항목 수정 완료";
    }

    // 사용자 비밀번호 변경
    public String changePassword(String uid, String currentPassword, String newPassword) throws FirebaseAuthException {
        FirebaseAuth auth = FirebaseAuth.getInstance();
        try {
            UserRecord.UpdateRequest request = new UserRecord.UpdateRequest(uid)
                    .setPassword(newPassword); // 새 비밀번호로 업데이트
            auth.updateUser(request);
            return "비밀번호 변경 완료";
        } catch (FirebaseAuthException e) {
            throw e;
        }
    }

    // 모든 사용자의 UID 목록을 조회하는 메서드
    public List<String> getAllUserUids() throws ExecutionException, InterruptedException {
        Firestore db = FirestoreClient.getFirestore();
        List<String> uids = new ArrayList<>();
        db.collection(COLLECTION_NAME).listDocuments().forEach(docRef -> uids.add(docRef.getId()));
        return uids;
    }
}