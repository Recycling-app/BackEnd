package com.example.recycling_app.controller;

import com.example.recycling_app.domain.WasteGuide;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;

@RestController // 이 클래스가 REST 컨트롤러임을 나타냅니다.
@RequestMapping("/api/waste-guides") // 이 컨트롤러의 기본 URL 경로를 설정합니다.
public class WasteGuideController {

    private final WasteGuideService wasteGuideService;

    // 생성자 주입을 통해 WasteGuideService를 사용합니다.
    public WasteGuideController(WasteGuideService wasteGuideService) {
        this.wasteGuideService = wasteGuideService;
    }

    /**
     * 특정 지역의 분리수거 가이드 정보를 반환하는 REST API 엔드포인트입니다.
     * HTTP GET 요청: GET http://localhost:8080/api/waste-guides?region=서울
     * 이 API는 백엔드 내부에서 사용되거나, 안드로이드 앱 외의 다른 클라이언트가 사용하도록 설계될 수 있습니다.
     * (현재 안드로이드 앱은 Firestore에서 직접 데이터를 가져오므로, 앱에서는 이 API를 직접 호출하지 않습니다.)
     *
     * @param region 쿼리 파라미터로 전달되는 지역 이름 (예: "서울", "부산")
     * @return WasteGuide 객체 (JSON 형식) 또는 404 Not Found 응답
     */
    @GetMapping // HTTP GET 요청을 처리하는 메서드입니다.
    public ResponseEntity<WasteGuide> getWasteGuideByRegion(@RequestParam String region) {
        // WasteGuideService를 호출하여 Firestore에서 해당 지역의 가이드 정보를 가져옵니다.
        Optional<WasteGuide> wasteGuide = wasteGuideService.getWasteGuideByRegion(region);

        // 결과에 따라 적절한 HTTP 응답을 반환합니다.
        return wasteGuide.map(ResponseEntity::ok) // 가이드가 존재하면 200 OK와 함께 WasteGuide 객체를 반환
                .orElseGet(() -> ResponseEntity.notFound().build()); // 가이드가 없으면 404 Not Found 응답 반환
    }
}
