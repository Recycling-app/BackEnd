package com.example.recycling_app.controller;

import com.example.recycling_app.dto.GeminiRequest;
import com.example.recycling_app.service.GeminiService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/gemini")
public class GeminiController {

    private final GeminiService geminiService;

    public GeminiController(GeminiService geminiService) {
        this.geminiService = geminiService;
    }

    @PostMapping("/ask")
    public String askGemini(@RequestBody GeminiRequest request) {
        // 안드로이드 앱에서 받은 classification 값을 서비스로 전달
        return geminiService.getRecyclingInfoFromGemini(request.getClassification());
    }
}