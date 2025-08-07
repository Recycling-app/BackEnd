package com.example.recycling_app.controller;

import com.example.recycling_app.dto.ProfileDTO;
import com.example.recycling_app.service.ProfileService;
import com.example.recycling_app.util.FirebaseTokenVerifier;
import com.google.firebase.auth.FirebaseAuthException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import lombok.extern.slf4j.Slf4j; // 로깅을 위한 Lombok 어노테이션 추가

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ExecutionException;

// 사용자 프로필 관련 요청을 처리하는 REST 컨트롤러
// 지원 기능: 프로필 조회, 전체 저장, 이미지 업로드, 부분 수정, 계정 삭제, 비밀번호 변경 등
@RestController
@RequestMapping("/profile") // 기본 URL 경로: /profile
@Slf4j // 로깅 활성화
public class ProfileController {

    @Autowired
    private ProfileService profileService; // 프로필 서비스 의존성 주입

    @Autowired
    private FirebaseTokenVerifier firebaseTokenVerifier; // Firebase 토큰 검증기

    // UID를 기반으로 사용자 프로필 정보 조회
    @GetMapping("/{uid}")
    public ResponseEntity<?> getProfile(@PathVariable String uid) {
        try {
            ProfileDTO profile = profileService.getProfile(uid); // 프로필 서비스에서 해당 uid의 정보 반환
            return ResponseEntity.ok(profile); // 성공 시 프로필 DTO 반환
        } catch (IllegalArgumentException e) {
            // 해당 UID에 대한 프로필이 존재하지 않는 경우
            log.warn("프로필 조회 실패 (UID: {}): {}", uid, e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (InterruptedException | ExecutionException e) {
            // Firestore 통신 또는 비동기 작업 중 오류 발생
            log.error("프로필 조회 중 서버 오류 발생 (UID: {}): {}", uid, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("프로필 조회 중 서버 오류가 발생했습니다.");
        } catch (Exception e) {
            // 그 외 예상치 못한 오류
            log.error("예상치 못한 오류로 프로필 조회 실패 (UID: {}): {}", uid, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("알 수 없는 오류로 프로필 조회를 실패했습니다.");
        }
    }

    // 사용자 프로필 저장 (전체 저장)
    // Authorization 헤더의 Bearer 토큰을 검증하여 사용자 인증
    @PutMapping("/{uid}")
    public ResponseEntity<String> saveProfile(
            @PathVariable String uid,
            @RequestHeader("Authorization") String authorizationHeader,
            @RequestBody ProfileDTO profileDTO
    ) {
        try {
            String token = authorizationHeader.replace("Bearer ", ""); // "Bearer " 접두사 제거
            String verifiedUid = firebaseTokenVerifier.verifyIdToken(token);   // 토큰 검증 후 UID 추출

            if (!uid.equals(verifiedUid)) {
                log.warn("프로필 저장 시도 - 권한 없음: 요청 UID '{}', 토큰 UID '{}'", uid, verifiedUid);
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("권한이 없습니다."); // 인증 실패
            }

            profileService.saveProfile(uid, profileDTO); // 프로필 전체 저장
            log.info("프로필 저장 성공 (UID: {})", uid);
            return ResponseEntity.ok("프로필 저장 성공");
        } catch (FirebaseAuthException e) {
            // Firebase 토큰 검증 실패 (예: 토큰 만료, 유효하지 않은 토큰)
            log.error("Firebase 인증 오류 - 프로필 저장 (UID: {}): {}", uid, e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("인증 실패: 유효하지 않은 토큰입니다.");
        } catch (InterruptedException | ExecutionException e) {
            // Firestore 작업 중 발생할 수 있는 예외 (비동기 작업 관련)
            log.error("Firestore 작업 중 오류 - 프로필 저장 (UID: {}): {}", uid, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("프로필 저장 중 서버 오류가 발생했습니다.");
        } catch (Exception e) {
            // 위에서 명시적으로 처리되지 않은 모든 다른 예외
            log.error("예상치 못한 오류 - 프로필 저장 (UID: {}): {}", uid, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("알 수 없는 오류로 프로필 저장을 실패했습니다.");
        }
    }

    // 사용자 계정 삭제 (회원 탈퇴)
    @DeleteMapping("/{uid}")
    public ResponseEntity<String> deleteUserAccount(
            @PathVariable String uid,
            @RequestHeader("Authorization") String authorizationHeader // Authorization 헤더 추가
    ) {
        try {
            String token = authorizationHeader.replace("Bearer ", "");
            String verifiedUid = firebaseTokenVerifier.verifyIdToken(token); // 토큰 검증

            if (!uid.equals(verifiedUid)) {
                log.warn("계정 삭제 시도 - 권한 없음: 요청 UID '{}', 토큰 UID '{}'", uid, verifiedUid);
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("권한이 없습니다."); // 인증 실패
            }

            String result = profileService.deleteUserAccount(uid); // 계정 삭제 처리
            log.info("사용자 계정 삭제 완료 (UID: {})", uid);
            return ResponseEntity.ok(result);
        } catch (FirebaseAuthException e) {
            // Firebase 토큰 검증 실패 (예: 토큰 만료, 유효하지 않은 토큰)
            log.error("Firebase 인증 오류 - 계정 삭제 (UID: {}): {}", uid, e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("인증 실패: 유효하지 않은 토큰입니다.");
        } catch (InterruptedException | ExecutionException e) {
            // Firestore 또는 Firebase Auth 작업 중 오류 발생
            log.error("계정 삭제 중 서버 오류 (UID: {}): {}", uid, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("회원 탈퇴 처리 중 서버 오류가 발생했습니다.");
        } catch (Exception e) {
            // 그 외 예상치 못한 오류
            log.error("예상치 못한 오류 - 계정 삭제 (UID: {}): {}", uid, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("알 수 없는 오류로 회원 탈퇴를 실패했습니다.");
        }
    }

    // 프로필 이미지 업로드 및 URL 저장
    // Storage에 파일 업로드 후 해당 URL을 프로필에 저장
    @PostMapping("/{uid}/upload-image")
    public ResponseEntity<String> uploadImage(
            @PathVariable String uid,
            @RequestHeader("Authorization") String authorizationHeader, // Authorization 헤더 추가
            @RequestParam("file") MultipartFile file // 업로드할 이미지 파일
    ) {
        try {
            String token = authorizationHeader.replace("Bearer ", "");
            String verifiedUid = firebaseTokenVerifier.verifyIdToken(token);

            if (!uid.equals(verifiedUid)) {
                log.warn("이미지 업로드 시도 - 권한 없음: 요청 UID '{}', 토큰 UID '{}'", uid, verifiedUid);
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("권한이 없습니다.");
            }

            if (file.isEmpty()) {
                log.warn("이미지 업로드 실패 (UID: {}): 파일이 비어있습니다.", uid);
                return ResponseEntity.badRequest().body("업로드할 파일이 없습니다.");
            }

            // 이미지 업로드 후 Firestore에 URL 저장 (saveToFirestore = true)
            String imageUrl = profileService.uploadProfileImage(uid, file, true);
            log.info("프로필 이미지 업로드 성공 (UID: {}), URL: {}", uid, imageUrl);
            return ResponseEntity.ok(imageUrl); // 업로드된 이미지 URL 응답
        } catch (FirebaseAuthException e) {
            log.error("Firebase 인증 오류 - 이미지 업로드 (UID: {}): {}", uid, e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("인증 실패: 유효하지 않은 토큰입니다.");
        } catch (IOException e) {
            // 파일 처리 중 발생할 수 있는 IO 오류
            log.error("이미지 파일 처리 중 오류 발생 (UID: {}): {}", uid, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("이미지 파일 처리 중 오류가 발생했습니다.");
        } catch (InterruptedException | ExecutionException e) {
            // Firestore 또는 Storage 통신 중 오류 발생
            log.error("Storage/Firestore 작업 중 오류 - 이미지 업로드 (UID: {}): {}", uid, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("이미지 업로드 중 서버 오류가 발생했습니다.");
        } catch (Exception e) {
            log.error("예상치 못한 오류 - 이미지 업로드 (UID: {}): {}", uid, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("알 수 없는 오류로 이미지 업로드를 실패했습니다.");
        }
    }

    // 프로필 정보 중 일부 필드만 수정 (부분 업데이트)
    // 전달받은 Map<String, Object> 필드만 수정됨
    @PatchMapping("/{uid}")
    public ResponseEntity<String> updateProfileFields(
            @PathVariable String uid,
            @RequestHeader("Authorization") String authorizationHeader, // Authorization 헤더 추가
            @RequestBody Map<String, Object> updates // 수정할 필드들 (예: nickname, age 등)
    ) {
        try {
            String token = authorizationHeader.replace("Bearer ", "");
            String verifiedUid = firebaseTokenVerifier.verifyIdToken(token); // 토큰 검증

            if (!uid.equals(verifiedUid)) {
                log.warn("프로필 일부 수정 시도 - 권한 없음: 요청 UID '{}', 토큰 UID '{}'", uid, verifiedUid);
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("권한이 없습니다."); // 인증 실패
            }

            if (updates == null || updates.isEmpty()) {
                log.warn("프로필 일부 수정 실패 (UID: {}): 업데이트할 내용이 없습니다.", uid);
                return ResponseEntity.badRequest().body("업데이트할 프로필 필드가 없습니다.");
            }

            String result = profileService.updateProfileFields(uid, updates); // 일부 필드만 업데이트
            log.info("프로필 일부 항목 수정 완료 (UID: {})", uid);
            return ResponseEntity.ok(result);
        } catch (FirebaseAuthException e) {
            log.error("Firebase 인증 오류 - 프로필 일부 수정 (UID: {}): {}", uid, e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("인증 실패: 유효하지 않은 토큰입니다.");
        } catch (InterruptedException | ExecutionException e) {
            log.error("Firestore 작업 중 오류 - 프로필 일부 수정 (UID: {}): {}", uid, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("프로필 일부 수정 중 서버 오류가 발생했습니다.");
        } catch (Exception e) {
            log.error("예상치 못한 오류 - 프로필 일부 수정 (UID: {}): {}", uid, e.getMessage(), e);
            return ResponseEntity.status(500).body("알 수 없는 오류로 프로필 일부 수정을 실패했습니다.");
        }
    }

    // 사용자 비밀번호 변경
    @PatchMapping("/{uid}/password")
    public ResponseEntity<String> changePassword(
            @PathVariable String uid,
            @RequestHeader("Authorization") String authorizationHeader, // 요청을 보낸 사용자의 인증 토큰
            @RequestBody Map<String, String> passwordUpdate // currentPassword와 newPassword 포함
    ) {
        try {
            String token = authorizationHeader.replace("Bearer ", "");
            String verifiedUid = firebaseTokenVerifier.verifyIdToken(token); // 토큰 검증

            if (!uid.equals(verifiedUid)) {
                log.warn("비밀번호 변경 시도 - 권한 없음: 요청 UID '{}', 토큰 UID '{}'", uid, verifiedUid);
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("권한이 없습니다.");
            }

            String currentPassword = passwordUpdate.get("currentPassword");
            String newPassword = passwordUpdate.get("newPassword");

            if (currentPassword == null || currentPassword.isEmpty() || newPassword == null || newPassword.isEmpty()) {
                log.warn("비밀번호 변경 실패 (UID: {}): 현재 비밀번호 또는 새 비밀번호가 누락되었습니다.", uid);
                return ResponseEntity.badRequest().body("현재 비밀번호와 새 비밀번호를 모두 입력해야 합니다.");
            }

            // ProfileService의 changePassword 메서드 호출
            // ProfileService에서는 currentPassword를 직접 검증하지 않고, newPassword로 업데이트만 수행합니다.
            // 클라이언트 측에서 Firebase Authentication의 reauthenticateWithCredential()을 통해 현재 비밀번호를 검증하는 것이 더 안전합니다.
            profileService.changePassword(uid, currentPassword, newPassword);
            log.info("비밀번호 변경 성공 (UID: {})", uid);
            return ResponseEntity.ok("비밀번호 변경 성공");
        } catch (FirebaseAuthException e) {
            // Firebase Authentication 관련 오류 (예: 약한 비밀번호, 사용자 없음 등)
            log.error("비밀번호 변경 실패 - Firebase Auth 오류 (UID: {}): {}", uid, e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("비밀번호 변경 실패: " + e.getMessage());
        } catch (IllegalArgumentException e) {
            // 서비스 계층에서 발생할 수 있는 잘못된 인자 오류
            log.error("비밀번호 변경 실패 - 잘못된 인자 (UID: {}): {}", uid, e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            // 그 외 예상치 못한 오류
            log.error("예상치 못한 오류 - 비밀번호 변경 (UID: {}): {}", uid, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("비밀번호 변경 실패: " + e.getMessage());
        }
    }
}