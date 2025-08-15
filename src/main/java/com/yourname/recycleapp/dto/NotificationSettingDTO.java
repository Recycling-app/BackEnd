package com.yourname.recycleapp.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

// 사용자 알림 설정 정보를 담는 DTO
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class NotificationSettingDTO {
    // 분리수거 알림 설정
    private boolean recyclingNotificationEnabled;
    private int daysBefore;
    private String notificationTime;

    // 광고/프로모션 알림 설정
    private boolean adNotificationEnabled;

    // 공지사항 등 정보성 알림 설정
    private boolean infoNotificationEnabled;
}