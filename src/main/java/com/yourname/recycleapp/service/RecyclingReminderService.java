package com.yourname.recycleapp.service;

import com.yourname.recycleapp.dto.NotificationSettingDTO;
import com.yourname.recycleapp.dto.RecyclingScheduleDTO;
import com.yourname.recycleapp.service.FcmPushService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.ExecutionException;

@Service
public class RecyclingReminderService {

    @Autowired
    private RecyclingScheduleService recyclingScheduleService;

    @Autowired
    private FcmPushService fcmPushService;

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private ProfileService profileService;

    // 매일 자정(0시 0분)에 실행되는 스케줄링 메서드
    @Scheduled(cron = "0 0 0 * * *")
    public void checkForRecyclingDay() {
        // 모든 사용자의 프로필을 조회
        List<String> allUids = getAllUids(); // 모든 사용자 UID를 가져오는 메서드가 필요합니다.

        for (String uid : allUids) {
            try {
                NotificationSettingDTO notificationSetting = notificationService.getNotificationSetting(uid);

                if (notificationSetting != null && notificationSetting.isRecyclingNotificationEnabled()) {
                    // 사용자 프로필에서 주소 정보 가져오기
                    String userAddress = profileService.getProfile(uid).getAddress();
                    String[] addressParts = userAddress.split(" ");
                    String sidoName = addressParts[0];
                    String sigunguName = addressParts[1];

                    RecyclingScheduleDTO schedule = recyclingScheduleService.getScheduleByRegion(sidoName, sigunguName);

                    LocalDate today = LocalDate.now();
                    DayOfWeek todayDayOfWeek = today.getDayOfWeek();

                    if (schedule != null && todayDayOfWeek.toString().equals(schedule.getRecyclingDay())) {
                        String title = "분리수거 알림";
                        String body = schedule.getDescription() + " 오늘 배출해주세요.";
                        // fcmPushService.sendPushNotification(uid, title, body);
                    }
                }
            } catch (ExecutionException | InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private List<String> getAllUids() {
        // TODO: Firestore users 컬렉션에서 모든 UID를 가져오는 로직 구현 필요
        return List.of("user_uid_1", "user_uid_2"); // 임시 UID 리스트
    }
}