package com.example.recycling_app.service;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.cloud.StorageClient;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.PostConstruct;
import java.io.InputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

@Service
public class FirebaseStorageService {

    @PostConstruct
    public void initialize() throws Exception {
        ClassPathResource resource = new ClassPathResource("firebase/firebase-service-account.json");
        try (InputStream serviceAccount = resource.getInputStream()) {
            FirebaseOptions options = FirebaseOptions.builder()
                    .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                    .setStorageBucket("your-name-382bf.firebasestorage.app")
                    .build();
            if (FirebaseApp.getApps().isEmpty()) {
                FirebaseApp.initializeApp(options);
            }
        }
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