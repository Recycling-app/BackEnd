package com.example.recycling_app.service;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.storage.Blob;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.cloud.StorageClient;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.PostConstruct;
import java.io.InputStream;
import java.net.URLEncoder;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

@Service("communityFirebaseStorageService")
public class FirebaseStorageService {

    private static final String BUCKET_NAME = "your-project-id.appspot.com";

    @PostConstruct
    public void initialize() throws Exception {
        ClassPathResource resource = new ClassPathResource("firebase/firebase-service-account.json");
        try (InputStream serviceAccount = resource.getInputStream()) {
            FirebaseOptions options = FirebaseOptions.builder()
                    .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                    .setStorageBucket(BUCKET_NAME)
                    .setStorageBucket("your-name-382bf.firebasestorage.app")
                    .build();
            if (FirebaseApp.getApps().isEmpty()) {
                FirebaseApp.initializeApp(options);
            }
        }
    }

    /**
     * 폴더 이름이 없는 경우, 메인 메소드에 null을 전달하여 호출
     */
    public String uploadFile(MultipartFile file) throws Exception {
        // 중복 로직을 없애고 아래의 메인 메소드를 호출하도록 변경
        return this.uploadFile(file, null);
    }

    // MultipartFile을 Firebase Storage의 지정된 폴더에 업로드하고 공개 URL을 반환합니다.
    public String uploadFile(MultipartFile file, String folderName) throws Exception {
        // 파일 이름 생성 (코드를 한 곳에서 관리)
        String fileName = UUID.randomUUID().toString() + "-" + file.getOriginalFilename();

        String fullPath;

        // folderName 유무에 따라 경로 설정
        if (StringUtils.hasText(folderName)) {
            fullPath = folderName.trim() + "/" + fileName;
        } else {
            fullPath = fileName;
        }

        // 파일 업로드
        StorageClient.getInstance().bucket().create(fullPath, file.getBytes(), file.getContentType());

        // URL 반환 (BUCKET_NAME 상수를 사용하여 일관성 유지)
        return "https://firebasestorage.googleapis.com/v0/b/" + BUCKET_NAME + "/o/" +
                java.net.URLEncoder.encode(fullPath, "UTF-8") + "?alt=media";
    }

    // Firebase Storage에서 파일을 삭제합니다.
    public void deleteFile(String fileUrl) throws Exception {
        // fileUrl에서 버킷 내부 파일 경로(fullPath)를 추출합니다.
        var bucket = StorageClient.getInstance().bucket();
        String encodedFullPath = fileUrl.split("/o/")[1].split("\\?")[0];
        String fullPath = URLDecoder.decode(encodedFullPath, StandardCharsets.UTF_8);

        Blob blob = bucket.get(fullPath);

        if (blob != null && blob.exists()) {
            blob.delete();
            System.out.println("Storage 파일 삭제 성공: " + fullPath);
        } else {
            System.out.println("Storage 파일이 존재하지 않음: " + fullPath);
        }
    public String uploadFile(MultipartFile file) throws Exception{
        String fileName = UUID.randomUUID().toString()+"-"+file.getOriginalFilename();
        StorageClient.getInstance().bucket().create(fileName, file.getBytes(), file.getContentType());
        String encodedFileName = URLEncoder.encode(fileName, StandardCharsets.UTF_8.toString());
        encodedFileName = encodedFileName.replace("+", "%20");
        // URL 포맷: https://firebasestorage.googleapis.com/v0/b/your-project-id.appspot.com/o/{파일명}?alt=media
        return "https://firebasestorage.googleapis.com/v0/b/your-name-382bf.firebasestorage.app/o/" +
                encodedFileName + "?alt=media";
    }
}