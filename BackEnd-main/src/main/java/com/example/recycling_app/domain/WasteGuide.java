package com.example.recycling_app.domain;

import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Getter // 모든 필드에 대한 getter 메서드를 자동으로 생성합니다.
@Setter // 모든 필드에 대한 setter 메서드를 자동으로 생성합니다.
@NoArgsConstructor // 인자 없는 기본 생성자를 자동으로 생성합니다. (Firebase가 객체 변환 시 필요)
@AllArgsConstructor // 모든 필드를 인자로 받는 생성자를 자동으로 생성합니다.
public class WasteGuide {

    // Firestore 문서의 필드 이름과 이 클래스의 필드 이름이 정확히 일치해야 합니다.
    // (Firebase SDK가 자동으로 매핑합니다.)
    private String regionName;          // 지역명 (예: "서울", "부산")
    private String generalWasteInfo;    // 일반 쓰레기 배출 정보
    private String recyclablesInfo;     // 재활용품 배출 정보
    private String foodWasteInfo;       // 음식물 쓰레기 배출 정보
    private String largeWasteInfo;      // 대형 폐기물 배출 정보 (지역별 상이)
    private String specialWasteInfo;    // 특수 폐기물 배출 정보 (예: 건전지, 형광등)
    private String collectionSchedule;  // 수거 요일 및 시간 (지역별 상이)
    private String additionalNotes;     // 추가 참고 사항

    // 이 클래스에는 Firestore 문서 자체의 ID를 저장하는 필드를 포함하지 않았습니다.
    // 필요하다면 String id; 필드를 추가하고, Firestore에서 가져올 때 setId()를 사용하거나,
    // @Exclude 어노테이션을 사용하여 Firestore가 이 필드를 문서의 실제 필드로 간주하지 않도록 할 수 있습니다.
}
