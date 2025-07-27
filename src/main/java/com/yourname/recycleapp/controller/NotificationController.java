package com.yourname.recycleapp.controller;

import com.yourname.recycleapp.service.FcmPushService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

// 클라이언트의 푸시 알림 요청을 처리하는 컨트롤러 클래스
@RestController
@RequestMapping("/notification") // 기본 URL 경로: /notification
public class NotificationController {

    @Autowired
    private FcmPushService fcmPushService; // FCM을 통한 푸시 알림 전송 서비스

    // FCM 푸시 알림 전송 API
    @PostMapping("/send")
    public ResponseEntity<String> sendNotification(
            @RequestParam String targetToken, // 알림을 받을 기기의 FCM 토큰
            @RequestParam String title,       // 알림 제목
            @RequestParam String body         // 알림 내용
    ) {
        try {
            fcmPushService.sendPushMessage(targetToken, title, body); // 서비스 호출하여 FCM 푸시 알림 전송
            return ResponseEntity.ok("푸시 알림 전송 성공");            // 200 OK + 성공 메시지 반환
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("푸시 알림 전송 실패"); // 500 Internal Server Error + 실패 메시지
        }
    }
}
