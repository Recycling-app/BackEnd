package com.yourname.recycleapp.service;

import com.google.cloud.firestore.Firestore;
import com.google.firebase.cloud.FirestoreClient;
import com.yourname.recycleapp.dto.NotificationSettingDTO;
import org.springframework.stereotype.Service;

import java.util.concurrent.ExecutionException;

@Service
public class NotificationService {

    private static final String COLLECTION_NAME = "users";

    // 사용자 알림 설정 조회
    public NotificationSettingDTO getNotificationSetting(String uid) throws ExecutionException, InterruptedException {
        Firestore db = FirestoreClient.getFirestore();
        return db.collection(COLLECTION_NAME)
                .document(uid)
                .collection("settings")
                .document("notifications")
                .get()
                .get()
                .toObject(NotificationSettingDTO.class);
    }

    // 사용자 알림 설정 업데이트
    public void updateNotificationSetting(String uid, NotificationSettingDTO settingDTO) throws ExecutionException, InterruptedException {
        Firestore db = FirestoreClient.getFirestore();
        db.collection(COLLECTION_NAME)
                .document(uid)
                .collection("settings")
                .document("notifications")
                .set(settingDTO)
                .get();
    }
}