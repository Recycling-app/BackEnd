package com.example.recycling_app.service.market;

import com.example.recycling_app.dto.market.ProductDto;
import com.example.recycling_app.service.FirebaseStorageService;
import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.*;
import com.google.firebase.cloud.FirestoreClient;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

@Service
public class ProductService {

    public static final String COLLECTION_NAME = "products"; // Firestore 컬렉션 이름
    private final FirebaseStorageService firebaseStorageService;

    public ProductService(FirebaseStorageService firebaseStorageService) {
        this.firebaseStorageService = firebaseStorageService;
    }

    // 상품 등록
    public String registerProduct(ProductDto product, List<MultipartFile> imageFiles) throws Exception {
        Firestore db = FirestoreClient.getFirestore();

        // 이미지 파일들을 Firebase Storage에 업로드하고 URL 목록을 받습니다.
        List<String> imageUrls = new ArrayList<>();
        for (MultipartFile file : imageFiles) {
            String imageUrl = firebaseStorageService.uploadFile(file, "products_images");
            imageUrls.add(imageUrl);
        }
        // ProductDto에 이미지 URL 리스트를 설정합니다.
        product.setImages(imageUrls);

        // 상품 메타데이터를 Firestore에 저장합니다.
        String productId = UUID.randomUUID().toString();
        product.setProductId(productId);
        product.setCreatedAt(System.currentTimeMillis());

        ApiFuture<WriteResult> future = db.collection(COLLECTION_NAME).document(productId).set(product);
        System.out.println("Update time : " + future.get().getUpdateTime());

        return productId; // 생성된 상품의 ID를 반환
    }

    // 모든 상품 조회
    public List<ProductDto> getAllProducts() throws ExecutionException, InterruptedException {
        Firestore db = FirestoreClient.getFirestore();

        ApiFuture<QuerySnapshot> future = db.collection(COLLECTION_NAME)
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .get();

        List<QueryDocumentSnapshot> documents = future.get().getDocuments();
        List<ProductDto> products = new ArrayList<>();

        for (QueryDocumentSnapshot document : documents) {
            ProductDto product = document.toObject(ProductDto.class);
            products.add(product);
        }

        return products;
    }

    // 상품명으로 검색
    public List<ProductDto> searchProductsByName(String keyword) throws ExecutionException, InterruptedException {
        Firestore db = FirestoreClient.getFirestore();

        // 모든 상품을 가져와서 필터링
        ApiFuture<QuerySnapshot> future = db.collection(COLLECTION_NAME).get();

        List<QueryDocumentSnapshot> documents = future.get().getDocuments();
        List<ProductDto> products = new ArrayList<>();

        for (QueryDocumentSnapshot document : documents) {
            ProductDto product = document.toObject(ProductDto.class);
            // 상품명에 키워드가 포함된 경우 (대소문자 무시)
            if (product.getProductName().toLowerCase().contains(keyword.toLowerCase()) ||
                    product.getProductDescription().toLowerCase().contains(keyword.toLowerCase())) {
                products.add(product);
            }
        }

        return products;
    }

    // 상품 삭제
    public void deleteProduct(String productId, String uid) throws Exception {
        Firestore db = FirestoreClient.getFirestore();

        System.out.println("삭제 요청 - ProductId: " + productId + ", UID: " + uid);

        // 상품 정보 조회
        DocumentSnapshot document = db.collection(COLLECTION_NAME).document(productId).get().get();

        if (!document.exists()) {
            System.out.println("상품이 존재하지 않음: " + productId);
            throw new RuntimeException("상품을 찾을 수 없습니다.");
        }

        ProductDto product = document.toObject(ProductDto.class);
        System.out.println("상품 소유자 UID: " + product.getUid());
        System.out.println("요청자 UID: " + uid);

        // 상품 소유자 확인
        if (!product.getUid().equals(uid)) {
            System.out.println("권한 없음 - 소유자: " + product.getUid() + ", 요청자: " + uid);
            throw new RuntimeException("자신의 상품만 삭제할 수 있습니다.");
        }

        // 상품 삭제
        ApiFuture<WriteResult> future = db.collection(COLLECTION_NAME).document(productId).delete();
        System.out.println("Delete time : " + future.get().getUpdateTime());
        System.out.println("상품 삭제 완료: " + productId);
    }
}