package com.example.recycling_app.service;

import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.WriteBatch;
import com.example.recycling_app.domain.WasteGuide;
import jakarta.annotation.PostConstruct; // Spring Boot 3.x부터는 'javax' 대신 'jakarta' 사용
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit; // 타임아웃 설정을 위해 추가

@Service // 이 클래스가 스프링 서비스 컴포넌트임을 나타냅니다.
public class DataInitializer {

    private final Firestore firestore;

    // application.properties나 기타 설정에서 CSV 파일 경로를 주입받습니다.
    // 파일은 'src/main/resources/data/' 폴더에 'waste_guide_data.csv'로 저장되어야 합니다.
    @Value("classpath:data/waste_guide_data.csv")
    private Resource csvFile;

    // Firestore에서 사용할 컬렉션 이름입니다. 안드로이드 앱에서 데이터를 조회할 때도 이 이름과 일치해야 합니다.
    private static final String FIRESTORE_COLLECTION_NAME = "wasteGuides";

    public DataInitializer(Firestore firestore) {
        this.firestore = firestore;
    }

    @PostConstruct // 스프링 애플리케이션 시작 시 이 메서드를 자동으로 실행합니다.
    public void initData() {
        // 데이터가 이미 Firestore에 존재하는지 확인하여 중복 로드를 방지합니다.
        // 실제 운영 환경에서는 이 로직을 제거하거나, 별도의 관리자 API를 통해 수동으로 로드하는 것이 좋습니다.
        try {
            // Firestore 컬렉션에서 문서가 하나라도 있는지 확인 (최대 10초 대기)
            if (firestore.collection(FIRESTORE_COLLECTION_NAME).limit(1).get().get(10, TimeUnit.SECONDS).isEmpty()) {
                System.out.println("Firestore 컬렉션 '" + FIRESTORE_COLLECTION_NAME + "'이(가) 비어있습니다. 데이터 초기화를 시작합니다.");
                loadWasteGuideDataToFirestore();
            } else {
                System.out.println("Firestore 컬렉션 '" + FIRESTORE_COLLECTION_NAME + "'에 이미 데이터가 있습니다. 초기화를 건너뜀.");
            }
        } catch (Exception e) {
            System.err.println("Firestore 컬렉션 상태 확인 또는 데이터 초기화 중 오류 발생: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void loadWasteGuideDataToFirestore() {
        System.out.println("CSV 파일로부터 분리수거 가이드 데이터를 Firestore에 로드하는 중...");
        WriteBatch batch = firestore.batch(); // 대량 쓰기를 위한 일괄 쓰기(Batch) 객체 생성

        int recordCount = 0;
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(csvFile.getInputStream(), StandardCharsets.UTF_8));
             CSVParser csvParser = new CSVParser(reader, CSVFormat.DEFAULT.withFirstRecordAsHeader().withIgnoreHeaderCase().withTrim())) {

            for (CSVRecord csvRecord : csvParser) {
                try {
                    // ★ 중요: CSV 파일의 실제 헤더 이름과 여기에 명시된 문자열이 정확히 일치해야 합니다.
                    // 예시: "지역명", "일반쓰레기", "재활용품", "음식물", "대형폐기물", "특수폐기물", "수거요일시간", "참고사항"
                    String regionName = csvRecord.get("지역명");
                    String generalWaste = csvRecord.get("일반쓰레기");
                    String recyclables = csvRecord.get("재활용품");
                    String foodWaste = csvRecord.get("음식물");
                    String largeWaste = csvRecord.get("대형폐기물");
                    String specialWaste = csvRecord.get("특수폐기물");
                    String collectionSchedule = csvRecord.get("수거요일시간");
                    String additionalNotes = csvRecord.get("참고사항");

                    // WasteGuide 객체 생성
                    WasteGuide guide = new WasteGuide(
                            regionName, generalWaste, recyclables, foodWaste,
                            largeWaste, specialWaste, collectionSchedule, additionalNotes
                    );

                    // Firestore 문서 ID를 지역명으로 설정합니다.
                    // 이렇게 하면 안드로이드 앱에서 해당 지역명을 사용하여 문서를 직접 조회하기 편리합니다.
                    batch.set(firestore.collection(FIRESTORE_COLLECTION_NAME).document(regionName), guide);
                    recordCount++;

                    // Firebase Firestore의 일괄 쓰기 제한(500개)을 고려하여 일정 수량마다 커밋합니다.
                    // (400개마다 커밋하여 안전 마진을 둡니다.)
                    if (recordCount % 400 == 0) {
                        batch.commit().get(10, TimeUnit.SECONDS); // 배치 커밋 (최대 10초 대기)
                        batch = firestore.batch(); // 새로운 배치 시작
                        System.out.println("현재까지 " + recordCount + "개의 레코드 배치 커밋 완료.");
                    }
                } catch (IllegalArgumentException e) {
                    System.err.println("CSV 레코드 처리 중 오류 발생 (헤더 누락 등): " + e.getMessage() + " - 레코드: " + csvRecord);
                }
            }
            // 남아있는 배치(batch)가 있다면 모두 커밋합니다.
            if (recordCount % 400 != 0 || recordCount == 0) {
                batch.commit().get(10, TimeUnit.SECONDS);
            }
            System.out.println("Firestore에 분리수거 가이드 데이터 로드 완료: 총 " + recordCount + "개의 레코드.");

        } catch (IOException | InterruptedException | ExecutionException e) {
            System.err.println("Firestore로 분리수거 가이드 데이터 로드 실패: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
