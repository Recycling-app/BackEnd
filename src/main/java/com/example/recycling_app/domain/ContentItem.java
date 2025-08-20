package com.example.recycling_app.domain;

import lombok.Data;

@Data
public class ContentItem {
    private String type;       // text, image, video
    private String text;       // text 내용 (type=text일 때)
    private String mediaUrl;   // 이미지 또는 동영상 URL(type=image 또는 video일 때)
    private int order;         // 순서
}
