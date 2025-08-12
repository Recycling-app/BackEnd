package com.example.recycling_app.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class GeminiService {

    @Value("${gemini.api.key}")
    private String apiKey;

    @Value("${gemini.model:gemini-1.5-flash}")
    private String geminiModel; // 변수명 통일

    private static final String GEMINI_API_BASE_URL = "https://generativelanguage.googleapis.com/v1beta/models";

    /**
     * 분류 정보를 바탕으로 Gemini API에 분리수거 방법을 문의하고 결과를 반환합니다.
     *
     * @param classification 분리수거 품목 이름
     * @return Gemini API가 생성한 분리수거 방법 안내 텍스트
     */
    public String getRecyclingInfoFromGemini(String classification) {
        // ### 요청사항 1: 입력 검증 ###
        if (classification == null || classification.trim().isEmpty()) {
            log.warn("분류 정보가 비어있습니다.");
            return "분류 정보를 입력해주세요.";
        }

        try {
            // API 요청 URL 구성
            String url = String.format("%s/%s:generateContent?key=%s",
                    GEMINI_API_BASE_URL, geminiModel, apiKey);

            // ### 요청사항 2: 새로운 프롬프트 적용 ###
            String prompt = String.format(
                    "'%s'의 분리수거 방법을 알려주세요. 다음 조건을 반드시 지켜주세요:\n\n" +
                            "1. 단계별로 번호를 붙여서 설명해주세요\n" +
                            "2. 각 단계는 간결하고 명확한 문장으로 작성해주세요\n" +
                            "3. 특수문자나 마크다운 기호는 사용하지 마세요\n" +
                            "4. 각 단계 사이에 한 줄씩 띄어주세요\n" +
                            "5. 최대 3-4개 단계로 압축해서 핵심만 알려주세요\n" +
                            "6. 한국의 분리수거 기준에 맞춰 설명해주세요",
                    classification
            );

            // RestTemplate 인스턴스 생성
            RestTemplate restTemplate = new RestTemplate();

            // 요청 본문 생성
            Map<String, Object> requestBody = Map.of(
                    "contents", List.of(
                            Map.of("parts", List.of(
                                    Map.of("text", prompt)
                            ))
                    )
            );

            // HTTP 헤더 설정
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            // HTTP 요청 엔티티 생성
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

            // API 호출
            ResponseEntity<Map> response = restTemplate.exchange(
                    url, HttpMethod.POST, entity, Map.class);

            // 응답에서 텍스트 추출하여 반환
            return extractTextFromResponse(response.getBody());

        } catch (Exception e) {
            log.error("Gemini API 호출 중 오류 발생: classification={}, error={}", classification, e.getMessage());
            // 사용자에게 보여줄 수 있는 더 친절한 에러 메시지를 반환할 수 있습니다.
            return "분리수거 정보를 가져오는 중 오류가 발생했습니다. 잠시 후 다시 시도해주세요.";
        }
    }

    /**
     * Gemini API 응답 본문(JSON)에서 실제 텍스트 내용을 추출합니다.
     * @param responseBody API 응답 본문
     * @return 추출된 텍스트
     */
    private String extractTextFromResponse(Map<String, Object> responseBody) {
        try {
            List<Map<String, Object>> candidates =
                    (List<Map<String, Object>>) responseBody.get("candidates");

            if (candidates != null && !candidates.isEmpty()) {
                Map<String, Object> content =
                        (Map<String, Object>) candidates.get(0).get("content");

                List<Map<String, Object>> parts =
                        (List<Map<String, Object>>) content.get("parts");

                if (parts != null && !parts.isEmpty()) {
                    return (String) parts.get(0).get("text");
                }
            }
        } catch (Exception e) {
            log.error("Gemini API 응답 파싱 오류: {}", e.getMessage());
        }

        return "응답 내용을 처리할 수 없습니다.";
    }
}