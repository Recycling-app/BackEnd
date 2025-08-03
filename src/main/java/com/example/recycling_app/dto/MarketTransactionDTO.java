package com.example.recycling_app.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


// 마켓 거래 정보를 담는 DTO 클래스
// 거래의 제목, 설명, 이미지, 날짜 및 상태를 포함하여 클라이언트와 서버 간 데이터 전달에 사용됨
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class MarketTransactionDTO {
    private String transactionId;     // 거래 ID (Firestore 문서 ID 등으로 사용)
    private String title;             // 거래 제목
    private String description;       // 거래 상세 설명
    private String imageUrl;          // 상품 또는 거래 관련 이미지 URL
    private String transactionDate;   // 거래 일자 (예: "2025-07-21" 형식)
    private String status;            // 거래 상태 (예: "거래중", "완료" 등)
}
