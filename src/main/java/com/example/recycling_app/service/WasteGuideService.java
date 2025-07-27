package com.example.recycling_app.service;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.CollectionReference;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.QueryDocumentSnapshot;
import com.google.cloud.firestore.QuerySnapshot;
import com.example.recycling_app.domain.WasteGuide;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

/**
 * WasteGuide 데이터를 Firestore에서 조회하는 비즈니스 로직을 담당하는 서비스 클래스입니다.
 */
@Service
public class WasteGuideService {

    private final Firestore firestore;
    private static final String COLLECTION_NAME = "wasteGuides"; // Firestore 컬렉션 이름

    /**
     * Firestore 인스턴스를 주입받아 서비스 초기화합니다.
     * @param firestore Firestore 데이터베이스 인스턴스
     */
    @Autowired
    public WasteGuideService(Firestore firestore) {
        this.firestore = firestore;
    }

    /**
     * 모든 분리수거 가이드 데이터를 Firestore에서 조회합니다.
     * @return 모든 WasteGuide 객체의 리스트
     */
    public List<WasteGuide> getAllWasteGuides() {
        List<WasteGuide> wasteGuides = new ArrayList<>();
        CollectionReference collectionRef = firestore.collection(COLLECTION_NAME);
        ApiFuture<QuerySnapshot> future = collectionRef.get();

        try {
            List<QueryDocumentSnapshot> documents = future.get().getDocuments();
            for (QueryDocumentSnapshot document : documents) {
                // Firestore 문서 데이터를 WasteGuide 객체로 변환
                wasteGuides.add(document.toObject(WasteGuide.class));
            }
        } catch (InterruptedException | ExecutionException e) {
            System.err.println("Firestore에서 모든 WasteGuide 데이터를 가져오는 중 오류 발생: " + e.getMessage());
            e.printStackTrace();
        }
        return wasteGuides;
    }

    /**
     * 특정 지역(시도명 또는 시군구명)에 해당하는 분리수거 가이드 데이터를 Firestore에서 조회합니다.
     * @param region 조회할 지역명 (시도명 또는 시군구명)
     * @return 해당 지역의 WasteGuide 객체 리스트
     */
    public List<WasteGuide> getWasteGuidesByRegion(String region) {
        List<WasteGuide> wasteGuides = new ArrayList<>();
        CollectionReference collectionRef = firestore.collection(COLLECTION_NAME);

        // 시도명 또는 시군구명 필드를 기준으로 쿼리합니다.
        // Firestore의 OR 쿼리는 복잡하므로, 여기서는 두 가지 쿼리를 각각 실행하고 결과를 합칩니다.
        // 실제 운영 환경에서는 Firestore 인덱스 및 쿼리 최적화를 고려해야 합니다.
        ApiFuture<QuerySnapshot> futureSido = collectionRef.whereEqualTo("sidoName", region).get();
        ApiFuture<QuerySnapshot> futureSigungu = collectionRef.whereEqualTo("sigunguName", region).get();

        try {
            // 시도명으로 찾은 결과 추가
            List<QueryDocumentSnapshot> sidoDocs = futureSido.get().getDocuments();
            for (QueryDocumentSnapshot document : sidoDocs) {
                wasteGuides.add(document.toObject(WasteGuide.class));
            }

            // 시군구명으로 찾은 결과 추가 (중복 방지를 위해 Set 사용 후 List로 변환 가능)
            List<QueryDocumentSnapshot> sigunguDocs = futureSigungu.get().getDocuments();
            for (QueryDocumentSnapshot document : sigunguDocs) {
                // 이미 추가된 문서인지 확인하여 중복을 피할 수 있습니다.
                // WasteGuide에 equals/hashCode를 구현해야 정확한 중복 제거가 가능합니다.
                if (!wasteGuides.contains(document.toObject(WasteGuide.class))) {
                    wasteGuides.add(document.toObject(WasteGuide.class));
                }
            }
            // 최종적으로 중복을 제거합니다. (WasteGuide에 equals/hashCode 구현 필요)
            return wasteGuides.stream().distinct().collect(Collectors.toList());

        } catch (InterruptedException | ExecutionException e) {
            System.err.println("Firestore에서 지역별 WasteGuide 데이터를 가져오는 중 오류 발생: " + e.getMessage());
            e.printStackTrace();
        }
        return new ArrayList<>(); // 오류 발생 시 빈 리스트 반환
    }
}