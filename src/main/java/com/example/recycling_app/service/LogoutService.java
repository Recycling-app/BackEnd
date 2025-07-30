package com.example.recycling_app.service;

import com.example.recycling_app.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.Date;

@Service
@RequiredArgsConstructor
@Slf4j
public class LogoutService {

    private final JwtUtil jwtUtil;
    private final TokenBlacklistService tokenBlacklistService;

    public void logout(String accessToken, String refreshToken) {
        try {
            // 1. Access Token 검증 및 블랙리스트 추가
            if (accessToken != null && jwtUtil.validateToken(accessToken)) {
                Date accessTokenExpiry = jwtUtil.extractExpiration(accessToken);
                tokenBlacklistService.addTokenBlacklist(accessToken, accessTokenExpiry);
                log.info("Access Token이 블랙리스트에 추가됨");
            }

            // 2. Refresh Token이 있다면 블랙리스트 추가
            if (refreshToken != null && jwtUtil.validateToken(refreshToken)) {
                Date refreshTokenExpiry = jwtUtil.extractExpiration(refreshToken);
                tokenBlacklistService.addTokenBlacklist(refreshToken, refreshTokenExpiry);
                log.info("Refresh Token이 블랙리스트에 추가됨");
            }

        } catch (Exception e) {
            log.error("로그아웃 처리 중 오류: {}", e.getMessage());
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "로그아웃 처리 중 오류가 발생했습니다: " + e.getMessage()
            );
        }
    }

    // 토큰이 유효하고 블랙리스트에 없는지 확인
    public boolean isValidAndNotBlacklisted(String token) {
        return jwtUtil.validateToken(token) && !tokenBlacklistService.isBlacklisted(token);
    }
}
