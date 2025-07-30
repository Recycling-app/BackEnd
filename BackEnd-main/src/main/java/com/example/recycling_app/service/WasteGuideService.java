package com.example.recycling_app.service;

import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.DocumentSnapshot;
import com.example.recycling_app.domain.WasteGuide;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit; // 타임아웃 설정을 위해 추가

@Service // 이 클래스가 스프링 서비스 컴포넌트임을 나타냅니다.
public class WasteGuideService {

    private final Firestore firestore;
    private static final String COLLECTION_NAME = "wasteGuides"; // Firestore 컬렉션 이름

    public WasteGuideService(Firestore firestore) {
        this.firestore = firestore;
    }

    /**
     * 특정 지역의 분리수거 가이드 정보를 Firebase Firestore에서 조회합니다.
     *
     * @param regionName 조회할 지역의 이름 (예: "서울", "부산")
     * @return 해당 지역의 WasteGuide 객체를 Optional로 감싸서 반환 (데이터가 없으면 Optional.empty())
     */
    public Optional<WasteGuide> getWasteGuideByRegion(String regionName) {
        try {
            // Firestore 컬렉션에서 문서 ID가 regionName과 일치하는 문서를 직접 가져옵니다.
            // DataInitializer에서 문서 ID를 지역명으로 저장했으므로 이 방법이 가장 효율적입니다.
            DocumentSnapshot document = firestore.collection(COLLECTION_NAME)
                    .document(regionName)
                    .get()
                    .get(10, TimeUnit.SECONDS); // 최대 10초 대기

            if (document.exists()) {
                // 문서를 WasteGuide 객체로 변환하여 반환합니다.
                WasteGuide wasteGuide = document.toObject(WasteGuide.class);
                return Optional.of(wasteGuide);
            } else {
                System.out.println("Firestore에서 지역 [" + regionName + "]에 대한 가이드 문서를 찾을 수 없습니다.");
            }
        } catch (InterruptedException | ExecutionException e) {
            System.err.println("Firestore에서 분리수거 가이드 조회 중 오류 발생: " + e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
            System.err.println("분리수거 가이드 조회 중 예기치 않은 오류 발생: " + e.getMessage());
            e.printStackTrace();
        }
        return Optional.empty(); // 데이터가 없거나 오류 발생 시 빈 Optional 반환
    }
}
