package com.yourname.recycleapp.controller;

import com.yourname.recycleapp.dto.ProfileDTO;
import com.yourname.recycleapp.service.ProfileService;
import com.yourname.recycleapp.util.FirebaseTokenVerifier;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;
import java.util.concurrent.ExecutionException;

// 사용자 프로필 관련 요청을 처리하는 REST 컨트롤러
// 지원 기능: 프로필 조회, 전체 저장, 이미지 업로드, 부분 수정, 계정 삭제 등
@RestController
@RequestMapping("/profile") // 기본 URL 경로: /profile
public class ProfileController {

    @Autowired
    private ProfileService profileService; // 프로필 서비스 의존성 주입

    @Autowired
    private FirebaseTokenVerifier firebaseTokenVerifier; // Firebase 토큰 검증기

    // UID를 기반으로 사용자 프로필 정보 조회
    @GetMapping("/{uid}")
    public ProfileDTO getProfile(@PathVariable String uid) throws ExecutionException, InterruptedException {
        return profileService.getProfile(uid); // 프로필 서비스에서 해당 uid의 정보 반환
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
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("권한이 없습니다."); // 인증 실패
            }

            profileService.saveProfile(uid, profileDTO); // 프로필 전체 저장
            return ResponseEntity.ok("프로필 저장 성공");
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("프로필 저장 실패");
        }
    }

    // 사용자 계정 삭제 (회원 탈퇴)
    @DeleteMapping("/{uid}")
    public ResponseEntity<String> deleteUserAccount(@PathVariable String uid) {
        try {
            String result = profileService.deleteUserAccount(uid); // 계정 삭제 처리
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("회원 탈퇴 처리 실패");
        }
    }

    // 프로필 이미지 업로드 및 URL 저장
    // Storage에 파일 업로드 후 해당 URL을 프로필에 저장
    @PostMapping("/{uid}/upload-image")
    public ResponseEntity<String> uploadImage(
            @PathVariable String uid,
            @RequestParam("file") MultipartFile file // 업로드할 이미지 파일
    ) {
        try {
            String imageUrl = profileService.uploadProfileImage(uid, file, true); // 이미지 업로드 후 URL 반환
            ProfileDTO profile = profileService.getProfile(uid);     // 기존 프로필 정보 조회
            profile.setProfileImageUrl(imageUrl);                    // 프로필에 이미지 URL 설정
            profileService.saveProfile(uid, profile);                // 프로필 저장
            return ResponseEntity.ok(imageUrl);                      // 이미지 URL 응답
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("이미지 업로드 실패");
        }
    }

    // 프로필 정보 중 일부 필드만 수정 (부분 업데이트)
    // 전달받은 Map<String, Object> 필드만 수정됨
    @PatchMapping("/{uid}")
    public ResponseEntity<String> updateProfileFields(
            @PathVariable String uid,
            @RequestBody Map<String, Object> updates // 수정할 필드들 (예: nickname, age 등)
    ) {
        try {
            String result = profileService.updateProfileFields(uid, updates); // 일부 필드만 업데이트
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("프로필 일부 수정 실패");
        }
    }
}
