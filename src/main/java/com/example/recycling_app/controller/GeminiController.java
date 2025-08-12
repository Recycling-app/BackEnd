package com.example.recycling_app.controller;

import com.example.recycling_app.dto.GeminiRequest;
import com.example.recycling_app.dto.GeminiResponse;
import com.example.recycling_app.service.GeminiService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/gemini")
@CrossOrigin(origins = "*") // 모든 도메인에서의 접근 허용 (개발용)
public class GeminiController {

    private static final Logger logger = LoggerFactory.getLogger(GeminiController.class);

    private final GeminiService geminiService;

    public GeminiController(GeminiService geminiService) {
        this.geminiService = geminiService;
    }

    @PostMapping("/recycling-info")
    public ResponseEntity<GeminiResponse> getRecyclingInfo(@RequestBody GeminiRequest request) {
        try {
            logger.info("분리수거 정보 요청 받음: {}", request.getClassification());

            // 입력 검증
            if (request.getClassification() == null || request.getClassification().trim().isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(GeminiResponse.failure("분류 정보가 필요합니다."));
            }

            // 서비스 호출
            String recyclingInfo = geminiService.getRecyclingInfoFromGemini(request.getClassification());

            // 성공 응답
            return ResponseEntity.ok(GeminiResponse.success(recyclingInfo));

        } catch (Exception e) {
            logger.error("분리수거 정보 조회 중 오류 발생", e);
            return ResponseEntity.internalServerError()
                    .body(GeminiResponse.failure("서버 내부 오류가 발생했습니다."));
        }
    }

    // 서버 상태 확인용 엔드포인트
    @GetMapping("/health")
    public ResponseEntity<GeminiResponse> healthCheck() {
        return ResponseEntity.ok(GeminiResponse.success("서버가 정상적으로 작동중입니다."));
    }

    // 기존 엔드포인트 유지 (호환성)
    @PostMapping("/ask")
    public ResponseEntity<String> askGemini(@RequestBody GeminiRequest request) {
        try {
            String result = geminiService.getRecyclingInfoFromGemini(request.getClassification());
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            logger.error("Legacy API 요청 처리 중 오류 발생", e);
            return ResponseEntity.internalServerError().body("오류가 발생했습니다.");
        }
    }
}