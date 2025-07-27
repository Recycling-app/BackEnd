package com.example.recycling_app.controller;

import com.example.recycling_app.domain.WasteGuide;
import com.example.recycling_app.service.WasteGuideService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 분리수거 가이드 데이터를 위한 REST API 컨트롤러입니다.
 * 클라이언트의 요청을 받아 WasteGuideService를 통해 데이터를 처리하고 응답합니다.
 */
@RestController
@RequestMapping("/api/waste-guides") // 기본 API 경로
public class WasteGuideController {

    private final WasteGuideService wasteGuideService;

    /**
     * WasteGuideService를 주입받아 컨트롤러를 초기화합니다.
     * @param wasteGuideService WasteGuide 데이터를 처리하는 서비스
     */
    @Autowired
    public WasteGuideController(WasteGuideService wasteGuideService) {
        this.wasteGuideService = wasteGuideService;
    }

    /**
     * 모든 분리수거 가이드 데이터를 조회하거나, 특정 지역의 가이드를 조회합니다.
     *
     * GET /api/waste-guides
     * GET /api/waste-guides?region=서울특별시
     *
     * @param region (선택 사항) 조회할 지역명 (시도명 또는 시군구명)
     * @return 조회된 WasteGuide 객체 리스트와 HTTP 상태 코드
     */
    @GetMapping
    public ResponseEntity<List<WasteGuide>> getWasteGuides(@RequestParam(required = false) String region) {
        List<WasteGuide> wasteGuides;
        if (region != null && !region.trim().isEmpty()) {
            // 지역 파라미터가 있을 경우 지역별 조회
            wasteGuides = wasteGuideService.getWasteGuidesByRegion(region.trim());
            System.out.println("지역별 분리수거 가이드 조회: " + region + ", 결과 수: " + wasteGuides.size());
        } else {
            // 지역 파라미터가 없을 경우 전체 조회
            wasteGuides = wasteGuideService.getAllWasteGuides();
            System.out.println("전체 분리수거 가이드 조회, 결과 수: " + wasteGuides.size());
        }

        if (wasteGuides.isEmpty() && region != null) {
            // 특정 지역으로 조회했으나 결과가 없는 경우
            return new ResponseEntity<>(wasteGuides, HttpStatus.NOT_FOUND); // 404 Not Found
        } else if (wasteGuides.isEmpty()) {
            // 전체 조회했으나 결과가 없는 경우 (데이터 초기화 문제 등)
            return new ResponseEntity<>(wasteGuides, HttpStatus.NO_CONTENT); // 204 No Content
        }
        // 성공적으로 데이터 조회 시 200 OK
        return new ResponseEntity<>(wasteGuides, HttpStatus.OK);
    }
}