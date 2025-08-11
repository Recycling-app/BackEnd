package com.example.recycling_app.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class GeminiService {

    @Value("${gemini.api-key}")
    private String apiKey;

    private final RestTemplate restTemplate = new RestTemplate();
    private final String GEMINI_API_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-pro:generateContent?key=";

    public String getRecyclingInfoFromGemini(String classification) {
        // 제미니 API에 보낼 요청 URL
        String url = GEMINI_API_URL + apiKey;

        if (!url.startsWith("http://") && !url.startsWith("https://")) {
            throw new IllegalArgumentException("잘못된 URL 형식입니다: " + url);
        }

        // 프롬프트 생성
        String prompt = String.format(
                "'%s' 분리수거 방법을 알려주세요. 아래 조건들을 반드시 지켜주세요:\n" +
                        "단계별로 번호를 붙여 설명" +
                        "각 단계는 간결한 문장으로 작성" +
                        "특수문자나 기호는 사용하지 마세요." +
                        "각 단계 사이에 한 줄씩 띄어주세요." +
                        "방법은 최대 3개로 해서 압축해서 알려주세요",
                classification
        );

        // API 요청 본문
        Map<String, Object> requestBody = new HashMap<>();
        Map<String, Object> textContent = new HashMap<>();
        textContent.put("text", prompt);

        Map<String, Object> part = new HashMap<>();
        part.put("text", prompt);

        Map<String, Object> content = new HashMap<>();
        content.put("parts", new Object[]{part});

        requestBody.put("contents", new Object[]{content});

        // HTTP 헤더 설정
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        // HTTP 요청 엔티티
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

        // API 호출 및 응답 파싱
        try {
            Map<String, Object> response = restTemplate.postForObject(url, entity, Map.class);


            // 응답에서 결과 텍스트 추출 (JSON 구조에 맞게 수정)
            if (response != null && response.containsKey("candidates")) {
                List<Map<String, Object>> candidates = (List<Map<String, Object>>) response.get("candidates");
                if (!candidates.isEmpty()) {
                    Map<String, Object> firstCandidate = candidates.get(0);
                    Map<String, Object> contentPart = (Map<String, Object>) firstCandidate.get("content");
                    List<Map<String, Object>> parts = (List<Map<String, Object>>) contentPart.get("parts");
                    return (String) parts.get(0).get("text");
                }
            }
            return "정보를 찾을 수 없습니다.";
        } catch (Exception e) {
            e.printStackTrace();
            return "오류가 발생했습니다.";
        }
    }
}