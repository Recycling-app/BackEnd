//package com.example.recycling_app.domain;
//
//import lombok.*;
//import org.springframework.data.annotation.Id;
//
//import java.time.LocalDateTime;
//
//@Data
//@NoArgsConstructor
//@AllArgsConstructor
//@Builder
//public class Like {
//
//    @Id
//    private String id;
//
//    private String userId;
//    private String targetId; // 게시글 ID 또는 댓글 ID
//    private String targetType; // "POST" 또는 "COMMENT"
//
//    private LocalDateTime createdAt;
//
//    public static Like create(String userId, String targetId, String targetType) {
//        return Like.builder()
//                .userId(userId)
//                .targetId(targetId)
//                .targetType(targetType)
//                .createdAt(LocalDateTime.now())
//                .build();
//    }
//}
