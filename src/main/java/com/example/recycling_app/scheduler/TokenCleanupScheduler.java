package com.example.recycling_app.scheduler;

import com.example.recycling_app.service.TokenBlacklistService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class TokenCleanupScheduler {

    private final TokenBlacklistService tokenBlacklistService;

    // 매일 새벽 2시에 만료된 토큰 정리
    @Scheduled(cron = "0 0 2 * * ?")
    public void cleanupExpiredTokens() {
        log.info("만료된 블랙리스트 토큰 정리 시작");
        try {
            tokenBlacklistService.cleanupExpiredTokens();
            log.info("만료된 블랙리스트 토큰 정리 완료");
        } catch (Exception e) {
            log.error("토큰 정리 중 오류 발생: {}", e.getMessage());
        }
    }
}

