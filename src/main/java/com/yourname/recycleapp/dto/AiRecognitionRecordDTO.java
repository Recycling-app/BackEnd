package com.yourname.recycleapp.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


 //AI 인식 기록 정보를 담는 DTO (Data Transfer Object) 클래스
 //사용자가 업로드한 이미지와 AI가 인식한 결과 및 시간을 포함하며,
 //클라이언트 <-> 서버 간 데이터 전달 시 사용됨

@Data // Lombok: Getter/Setter, toString, equals, hashCode 자동 생성
@AllArgsConstructor // 모든 필드를 받는 생성자 자동 생성
@NoArgsConstructor // 기본 생성자 자동 생성
public class AiRecognitionRecordDTO {
    private String recognizatoinItem; // AI가 인식한 분리수거 항목 이름 (예: 플라스틱, 캔 등)
    private String imageUrl; // 사용자가 업로드한 이미지의 URL
    private long timestamp; // 인식이 이루어진 시간 (UNIX timestamp, 밀리초 단위)
}
