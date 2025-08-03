package com.example.recycling_app.service;

import com.example.recycling_app.dto.MarketTransactionDTO;
import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.QuerySnapshot;
import com.google.cloud.firestore.WriteResult;
import com.google.firebase.cloud.FirestoreClient;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

// 사용자 거래(market transaction) 기록을 저장하고 조회하는 서비스 클래스
@Service
public class MarketTransactionService {
    private static final String COLLECTION_NAME = "market_transactions"; // Firestore 거래 기록 컬렉션명

    // 거래 기록 저장
    // transactionId가 없으면 UUID 생성 후 지정
    public String saveTransaction(String uid, MarketTransactionDTO transactionDTO) throws ExecutionException, InterruptedException {
        Firestore db = FirestoreClient.getFirestore();

        if (transactionDTO.getTransactionId() == null || transactionDTO.getTransactionId().isEmpty()) {
            transactionDTO.setTransactionId(UUID.randomUUID().toString()); // 고유 ID 생성
        }

        ApiFuture<WriteResult> future = db.collection(COLLECTION_NAME)
                .document(uid)                                  // 사용자 문서
                .collection("transactions")         // 거래 서브컬렉션
                .document(transactionDTO.getTransactionId())    // 거래 문서 ID
                .set(transactionDTO);                           // 데이터 저장

        future.get(); // 저장 완료 대기
        return "거래 기록 저장 완료";
    }

    // 특정 사용자 UID의 모든 거래 기록 조회
    public List<MarketTransactionDTO> getTransactions(String uid) throws ExecutionException, InterruptedException {
        Firestore db = FirestoreClient.getFirestore();
        ApiFuture<QuerySnapshot> future = db.collection(COLLECTION_NAME)
                .document(uid)                                // 사용자 문서
                .collection("transactions")        // 거래 서브컬렉션
                .get();                                       // 전체 문서 조회

        List<MarketTransactionDTO> list = new ArrayList<>();
        for (DocumentSnapshot doc : future.get().getDocuments()) {
            list.add(doc.toObject(MarketTransactionDTO.class));  // 문서 → DTO 변환 후 리스트에 추가
        }
        return list;
    }
}
