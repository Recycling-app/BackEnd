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
import java.util.UUID;

@Service
public class FirebaseStorageService {

    @PostConstruct
    public void initialize() throws Exception {
        ClassPathResource resource = new ClassPathResource("firebase/firebase-service-account.json");
        try (InputStream serviceAccount = resource.getInputStream()) {
            FirebaseOptions options = FirebaseOptions.builder()
                    .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                    .setStorageBucket("your-project-id.appspot.com")
                    .build();
            if (FirebaseApp.getApps().isEmpty()) {
                FirebaseApp.initializeApp(options);
            }
        }
    }

    public String uploadFile(MultipartFile file) throws Exception{
        String fileName = UUID.randomUUID().toString()+"-"+file.getOriginalFilename();
        StorageClient.getInstance().bucket().create(fileName, file.getBytes(), file.getContentType());
        // URL 포맷: https://firebasestorage.googleapis.com/v0/b/your-project-id.appspot.com/o/{파일명}?alt=media
        return "https://firebasestorage.googleapis.com/v0/b/your-name-382bf.appspot.com/o/" +
                java.net.URLEncoder.encode(fileName, "UTF-8") + "?alt=media";
    }
}

