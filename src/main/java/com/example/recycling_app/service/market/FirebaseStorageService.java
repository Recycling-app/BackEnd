package com.example.recycling_app.service.market;

import com.google.cloud.storage.Blob;
import com.google.cloud.storage.Bucket;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.UUID;

@Service
public class FirebaseStorageService {

    private final Bucket bucket;

    public FirebaseStorageService(Bucket bucket) {
        this.bucket = bucket;
    }

    /**
     * MultipartFile을 Firebase Storage의 지정된 폴더에 업로드하고 공개 URL을 반환합니다.
     * @param file 업로드할 이미지 파일
     * @param folderName 파일을 저장할 폴더 이름
     * @return 공개적으로 접근 가능한 이미지 URL
     * @throws IOException
     */
    public String uploadFile(MultipartFile file, String folderName) throws IOException {
        // 파일 이름이 중복되지 않도록 UUID를 사용합니다.
        String originalFileName = file.getOriginalFilename();
        String uniqueFileName = UUID.randomUUID().toString() + "_" + originalFileName;

        // 폴더 경로를 포함한 전체 파일 경로를 생성합니다.
        String fullPath = folderName + "/" + uniqueFileName;

        // Storage에 파일 업로드
        // bucket.create() 메소드에 전체 경로를 전달합니다.
        Blob blob = bucket.create(fullPath, file.getInputStream(), file.getContentType());

        // 업로드된 파일에 대한 공개 URL을 생성하여 반환합니다.
        return String.format("https://storage.googleapis.com/%s/%s", bucket.getName(), fullPath);
    }
}