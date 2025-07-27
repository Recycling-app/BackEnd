package com.example.recycling_app.service;

import com.google.cloud.firestore.Firestore;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class TokenBlacklistService {

    private final Firestore firestore;
    private static final String BLACKLIST_COLLECTION = "token_blacklist";

    // 토큰을 블랙리스트에 추가
    public void addTokenBlacklist(String token, Date expiration) {
        try{
            Map<String, Object> blacklistEntry = new HashMap<>();
            blacklistEntry.put("token", token);
            blacklistEntry.put("blacklistedAt", new Date());
            blacklistEntry.put("expiration", expiration);

            // 토큰 해시를 문서 ID로 사용 (보안상 원본 토큰 저장 방지)
            String tokenHash = String.valueOf(Math.abs(token.hashCode()));

            firestore.collection(BLACKLIST_COLLECTION)
                    .document(tokenHash)
                    .set(blacklistEntry)
                    .get();

            log.info("토큰이 블랙리스트에 추가되었습니다: {}", tokenHash);
        } catch (Exception e) {
            log.error("토큰 블랙리스트 추가 실패: {}", e.getMessage());
            throw new RuntimeException("토큰 블랙리스트 추가 실패: " + e.getMessage());
        }
    }

    // 토큰이 블랙리스트에 있는지 확인
    public boolean isBlacklisted(String token) {
        try {
            String tokenHash = String.valueOf(Math.abs(token.hashCode()));
            return firestore.collection(BLACKLIST_COLLECTION)
                    .document(tokenHash)
                    .get()
                    .get()
                    .exists();
        } catch (Exception e) {
            log.error("블랙리스트 확인 중 오류: {}", e.getMessage());
            // 에러 발생 시 안전하게 false 반환 (토큰 유효하다고 가정)
            return false;
        }
    }

    // 만료된 블랙리스트 항목들을 정리
    public void cleanupExpiredTokens() {
        try {
            Date now = new Date();
            firestore.collection(BLACKLIST_COLLECTION)
                    .whereLessThan("expiresAt", now)
                    .get()
                    .get()
                    .forEach(doc -> {
                        try {
                            doc.getReference().delete().get();
                        } catch (Exception e) {
                            log.error("만료된 토큰 삭제 실패: {}", e.getMessage());
                        }
                    });
            log.info("만료된 블랙리스트 토큰 정리 완료");
        } catch (Exception e) {
            log.error("블랙리스트 정리 실패: {}", e.getMessage());
        }
    }
}
