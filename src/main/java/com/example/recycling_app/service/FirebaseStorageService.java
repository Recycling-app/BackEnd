package com.example.recycling_app.service;

import com.google.cloud.storage.Acl;
import com.google.cloud.storage.BlobId;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.Storage;
import com.google.firebase.cloud.StorageClient;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

@Service
public class FirebaseStorageService {

    private final Storage storage;

    // 생성자를 통해 Google Cloud Storage 클라이언트를 주입받습니다.
    public FirebaseStorageService(Storage storage) {
        this.storage = storage;
    }

    /**
     * 폴더 이름이 없는 경우, 메인 메소드에 null을 전달하여 호출
     */
    public String uploadFile(MultipartFile file) throws Exception {
        return this.uploadFile(file, null);
    }

    /**
     * MultipartFile을 Firebase Storage의 지정된 폴더에 업로드하고 공개 URL을 반환합니다.
     * @param file 클라이언트로부터 받은 MultipartFile
     * @param folderName 이미지를 저장할 Storage 내의 폴더 이름
     * @return 공개적으로 접근 가능한 고정 URL
     */
    public String uploadFile(MultipartFile file, String folderName) throws Exception {
        // 기본 버킷 이름을 가져옵니다.
        String bucketName = StorageClient.getInstance().bucket().getName();

        // 고유한 파일 이름을 생성합니다.
        String fileName = UUID.randomUUID().toString() + "-" + file.getOriginalFilename();
        String fullPath;

        // folderName 유무에 따라 전체 경로를 설정합니다.
        if (StringUtils.hasText(folderName)) {
            fullPath = folderName.trim() + "/" + fileName;
        } else {
            fullPath = fileName;
        }

        // --- 파일 업로드 로직 ---
        BlobId blobId = BlobId.of(bucketName, fullPath);
        BlobInfo blobInfo = BlobInfo.newBuilder(blobId)
                .setContentType(file.getContentType())
                .build();

        try (InputStream inputStream = file.getInputStream()) {
            storage.create(blobInfo, inputStream.readAllBytes());
        }

        // 1. 파일을 모든 사용자가 읽을 수 있도록 공개(Public)로 설정합니다.
        storage.createAcl(blobId, Acl.of(Acl.User.ofAllUsers(), Acl.Role.READER));

        // 2. 파일의 고정적인 공개 미디어 링크를 반환합니다.
        return storage.get(blobId).getMediaLink();
    }

    /**
     * Firebase Storage에서 파일을 삭제합니다.
     * @param fileUrl 삭제할 파일의 URL
     */
    public void deleteFile(String fileUrl) throws Exception {
        String bucketName = StorageClient.getInstance().bucket().getName();

        // mediaLink 형식의 URL에서 파일 경로를 추출합니다.
        String objectNameEncoded = fileUrl.split("/o/")[1].split("\\?")[0];
        String objectName = URLDecoder.decode(objectNameEncoded, StandardCharsets.UTF_8);

        BlobId blobId = BlobId.of(bucketName, objectName);
        boolean deleted = storage.delete(blobId);

        if (deleted) {
            System.out.println("Storage 파일 삭제 성공: " + objectName);
        } else {
            System.out.println("Storage 파일이 존재하지 않거나 삭제에 실패함: " + objectName);
        }
    }
}