package com.example.recycling_app.service;

import com.example.recycling_app.dto.JwtLoginResponse;
import com.example.recycling_app.util.JwtUtil;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseToken;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class GoogleAuthService {

    private final JwtUtil jwtUtil;

    /**
     * 구글 ID 토큰 검증 후 사용자 정보 추출 및 JWT 발급
     *
     * @param idToken 클라이언트에서 전달받은 Google OAuth ID 토큰
     * @return JwtLoginResponse
     */
    public JwtLoginResponse loginWithGoogle(String idToken) {
        try {
            // ✅ 1단계: ID 토큰 검증 (Firebase Admin SDK 검증)
            FirebaseToken decodedToken = verifyGoogleIdToken(idToken);

            String uid = decodedToken.getUid();
            String email = decodedToken.getEmail();

            // ✅ 2단계: JWT 발급
            String accessToken = jwtUtil.createAccessToken(uid, email);
            String refreshToken = jwtUtil.createRefreshToken(uid, email);

            // ✅ 3단계: JWT 응답 DTO 반환
            return JwtLoginResponse.builder()
                    .accessToken(accessToken)
                    .refreshToken(refreshToken)
                    .tokenType("Bearer")
                    .expiresIn(3600)
                    .build();

        } catch (FirebaseAuthException e) {
            // Firebase에서 토큰 검증 실패한 경우 → 401 Unauthorized 반환
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "유효하지 않은 Google ID Token입니다.");
        } catch (Exception e) {
            // 예상치 못한 예외 처리 → 500 Internal Server Error
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "서버 오류: " + e.getMessage());
        }
    }

    /**
     * Google ID Token 검증 수행 메서드
     *
     * @param idToken 클라이언트로부터 전달받은 토큰
     * @return FirebaseToken (uid, email, name 등 포함)
     * @throws FirebaseAuthException 토큰 만료, 위조 등 인증 실패 시 발생
     */
    private FirebaseToken verifyGoogleIdToken(String idToken) throws FirebaseAuthException {
        if (idToken == null || idToken.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "idToken은 필수입니다.");
        }

        // 실제 Firebase Admin SDK가 구글로 서명된 JWT를 검증함
        return FirebaseAuth.getInstance().verifyIdToken(idToken);
    }
}
