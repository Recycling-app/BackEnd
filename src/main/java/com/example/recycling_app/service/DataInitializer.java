package com.example.recycling_app.service;

import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.WriteBatch;
import com.google.cloud.firestore.QuerySnapshot;
import com.example.recycling_app.domain.WasteGuide;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import com.opencsv.exceptions.CsvException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * 애플리케이션 시작 시 CSV 파일에서 분리수거 가이드 데이터를 읽어
 * Firebase Firestore에 초기 데이터를 로드하는 컴포넌트입니다.
 * 컬렉션에 이미 데이터가 존재하면 로드를 건너김으로써 중복 로드를 방지합니다.
 */
@Component
public class DataInitializer {

    private final Firestore firestore;
    private static final String FIRESTORE_COLLECTION_NAME = "waste_guide_all";
    // 당신의 정확한 CSV 파일 경로와 이름: resources/data/waste_guide_data.csv
    private static final String CSV_FILE_NAME = "data/waste_guide_data.csv";

    /**
     * Firestore 인스턴스를 주입받아 DataInitializer를 초기화합니다.
     * @param firestore Firestore 데이터베이스 인스턴스
     */
    @Autowired
    public DataInitializer(Firestore firestore) {
        this.firestore = firestore;
    }

    /**
     * 애플리케이션 시작 후 자동으로 실행되는 메서드입니다.
     * Firestore 컬렉션에 데이터가 없는 경우에만 CSV 데이터를 로드합니다.
     */
    @PostConstruct
    public void initData() {
        try {
            // Firestore 컬렉션에 문서가 하나라도 있는지 확인하여 중복 로드를 방지합니다.
            // 10초 타임아웃을 설정하여 무한 대기를 방지합니다.
            boolean isCollectionEmpty = firestore.collection(FIRESTORE_COLLECTION_NAME)
                    .limit(1) // 문서 하나만 확인
                    .get()
                    .get(10, TimeUnit.SECONDS) // 10초 대기
                    .isEmpty();

            if (isCollectionEmpty) {
                System.out.println("Firestore 컬렉션 '" + FIRESTORE_COLLECTION_NAME + "'이 비어 있습니다. 초기화 시작 (데이터 로드)..");
                loadWasteGuideDataToFirestore(); // 데이터 로드 실행
                System.out.println("DataInitializer가 Firebase Firestore에 데이터를 로드 완료했습니다.");
            } else {
                System.out.println("Firestore 컬렉션 '" + FIRESTORE_COLLECTION_NAME + "'에 이미 데이터가 있습니다. 초기화를 건너김.");
            }
        } catch (TimeoutException e) {
            System.err.println("Firestore 컬렉션 상태 확인 중 타임아웃 발생. 네트워크 또는 Firestore 연결 문제일 수 있습니다: " + e.getMessage());
            e.printStackTrace();
        } catch (InterruptedException | ExecutionException e) {
            System.err.println("Firestore 컬렉션 상태 확인 중 오류 발생: " + e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
            System.err.println("예기치 않은 오류로 Firestore 컬렉션 상태 확인 실패: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * CSV 파일에서 데이터를 읽어 Firestore에 배치 쓰기로 저장합니다.
     */
    private void loadWasteGuideDataToFirestore() {
        try (CSVReader reader = new CSVReaderBuilder(new InputStreamReader(
                // resources 폴더 내의 CSV 파일 경로를 정확히 지정합니다.
                getClass().getClassLoader().getResourceAsStream(CSV_FILE_NAME), StandardCharsets.UTF_8))
                .withSkipLines(1) // CSV 첫 번째 줄(헤더) 건너뛰기
                .build()) {

            List<String[]> allRows = reader.readAll(); // 모든 CSV 행 읽기
            WriteBatch batch = firestore.batch(); // Firestore 배치 쓰기 객체 생성
            int recordCount = 0;

            // 선택 사항: 기존 컬렉션의 데이터를 모두 삭제하고 새로 넣고 싶다면 아래 주석을 해제하세요.
            // (주의: 이 코드를 활성화하면 매번 앱 시작 시 기존 데이터가 삭제됩니다.)
            /*
            System.out.println("기존 Firestore 컬렉션 '" + FIRESTORE_COLLECTION_NAME + "' 데이터 삭제 중...");
            QuerySnapshot existingDocs = firestore.collection(FIRESTORE_COLLECTION_NAME).get().get(10, TimeUnit.SECONDS);
            for (QueryDocumentSnapshot doc : existingDocs.getDocuments()) {
                batch.delete(doc.getReference());
            }
            batch.commit().get(10, TimeUnit.SECONDS); // 삭제 배치 실행
            batch = firestore.batch(); // 새 배치 시작
            System.out.println("기존 데이터 삭제 완료.");
            */

            for (String[] row : allRows) {
                // CSV 행의 컬럼 수가 WasteGuide 생성자에 필요한 13개와 일치하는지 확인
                if (row.length < 13) {
                    System.err.println("경고: CSV 행의 컬럼 수가 예상보다 적습니다. 건너김: " + String.join(", ", row));
                    continue;
                }

                // CSV 행 데이터를 WasteGuide 객체로 매핑
                WasteGuide wasteGuide = new WasteGuide(
                        row[1], // 시도명 (sidoName)
                        row[2], // 시군구명 (sigunguName)
                        row[3], // 동읍면 (dongeupmyeonName)
                        row[4], // 배출장소 (placeofischargeNmae)
                        row[5], // 배출장소유형 (emissionsiteType)
                        row[6], // 생활쓰레기배출방법 (generalWasteMethod)
                        row[7], // 음식물쓰레기배출방법 (foodWasteMethod)
                        row[8], // 재활용가능쓰레기배출방법 (recyclableWasteMethod)
                        row[9], // 일시적다량폐기물배출방법 (bulkyWasteMethod)
                        row[10], // 일시적다량폐기물배출장소 (bulkyWasteplaceName)
                        row[11] // 데이터기준일자 (databaseDate)
                );
                // Firestore 문서 ID를 UUID로 고유하게 생성하여 배치에 추가
                batch.set(firestore.collection(FIRESTORE_COLLECTION_NAME).document(UUID.randomUUID().toString()), wasteGuide);
                recordCount++;
            }

            // 모든 배치 작업을 실행하고 완료될 때까지 최대 10초 대기
            batch.commit().get(10, TimeUnit.SECONDS);
            System.out.println("Firestore에 분리수거 가이드 데이터 로드 완료: 총 " + recordCount + "개의 레코드.");

        } catch (IOException e) {
            System.err.println("CSV 파일 (" + CSV_FILE_NAME + ") 읽기 중 오류 발생: " + e.getMessage());
            e.printStackTrace();
        } catch (CsvException e) {
            System.err.println("CSV 파싱 중 오류 발생: " + e.getMessage());
            e.printStackTrace();
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            System.err.println("Firestore 데이터 로드 중 오류 발생 (배치 커밋 타임아웃 또는 실행 예외): " + e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
            System.err.println("예기치 않은 오류 발생: " + e.getMessage());
            e.printStackTrace();
        }
    }
}