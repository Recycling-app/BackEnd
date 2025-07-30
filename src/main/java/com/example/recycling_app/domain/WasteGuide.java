package com.example.recycling_app.domain;

import com.google.cloud.firestore.annotation.DocumentId;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
// import lombok.AllArgsConstructor; // 모든 필드를 포함하는 생성자를 자동으로 만들지만, 아래 수동 생성자와 충돌 가능성이 있어 주석 처리합니다.

@Getter
@Setter
@NoArgsConstructor // 기본 생성자
public class WasteGuide {

    @DocumentId // Firestore 문서 ID로 사용할 필드 (UUID로 자동 생성될 것임)
    private String id; // Firestore 문서의 고유 ID

    // CSV 파일의 헤더에 맞춰 13개의 필드를 정의합니다.
    // CSV의 첫 번째 컬럼인 "번호"는 여기서는 'csvNumber'로 명명하여 'id'와 분리합니다.
    private String csvNumber; // CSV의 "번호" (row[0])
    private String sidoName; // "시도명" (row[1])
    private String sigunguName; // "시군구명" (row[2])
    private String dongeupmyeonName; // "동읍면" (row[3])
    private String placeofischargeName; // "배출장소" (row[4])
    private String emissionsiteType; // "배출장소유형" (row[5])
    private String generalWasteMethod; // "생활쓰레기배출방법" (row[6])
    private String foodWasteMethod; // "음식물쓰레기배출방법" (row[7])
    private String recyclableWasteMethod; // "재활용가능쓰레기배출방법" (row[8])
    private String bulkyWasteMethod; // "일시적다량폐기물배출방법" (row[9])
    private String bulkyWasteplaceName; // "일시적다량폐기물배출장소" (row[10])
    private String databaseDate; // "데이터기준일자" (row[11])

    /**
     * CSV 파일의 13개 컬럼에 정확히 맞춰서 생성자를 수동으로 정의합니다.
     * @param csvNumber CSV 파일의 '번호' 컬럼 값
     * @param sidoName CSV 파일의 '시도명' 컬럼 값
     * @param sigunguName CSV 파일의 '시군구명' 컬럼 값
     * @param dongeupmyeonName CSV 파일의 '동읍면' 컬럼 값
     * @param placeofischargeName CSV 파일의 '배출장소' 컬럼 값
     * @param emissionsiteType CSV 파일의 '배출장소유형' 컬럼 값
     * @param generalWasteMethod CSV 파일의 '생활쓰레기배출방법' 컬럼 값
     * @param foodWasteMethod CSV 파일의 '음식물쓰레기배출방법' 컬럼 값
     * @param recyclableWasteMethod CSV 파일의 '재활용가능쓰레기배출방법' 컬럼 값
     * @param bulkyWasteMethod CSV 파일의 '일시적다량폐기물배출방법' 컬럼 값
     * @param bulkyWasteplaceName CSV 파일의 '일시적다량폐기물배출장소' 컬럼 값
     * @param databaseDate CSV 파일의 '데이터기준일자' 컬럼 값
     */
    public WasteGuide(String csvNumber, String sidoName, String sigunguName,
                      String dongeupmyeonName, String placeofischargeName, String emissionsiteType,
                      String generalWasteMethod, String foodWasteMethod, String recyclableWasteMethod,
                      String bulkyWasteMethod, String bulkyWasteplaceName, String databaseDate
                      ) {
        this.csvNumber = csvNumber;
        this.sidoName = sidoName;
        this.sigunguName = sigunguName;
        this.dongeupmyeonName = dongeupmyeonName;
        this.placeofischargeName = placeofischargeName;
        this.emissionsiteType = emissionsiteType;
        this.generalWasteMethod = generalWasteMethod;
        this.foodWasteMethod =  foodWasteMethod;
        this.recyclableWasteMethod = recyclableWasteMethod;
        this.bulkyWasteMethod = bulkyWasteMethod;
        this.bulkyWasteplaceName = bulkyWasteplaceName;
        this.databaseDate = databaseDate;
    }

    /**
     * '지역명' 필드는 CSV에 없으므로, 필요하다면 getter 메서드로 조합하여 사용합니다.
     * @return 시도명과 시군구명을 합친 지역명 문자열
     */
    public String get지역명() {
        return (this.sidoName != null ? this.sidoName : "") +
                (this.sigunguName != null ? " " + this.sigunguName : "");
    }
}
