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
//public class Comment {
//
//    @Id
//    private String id;
//
//    private String postId;
//    private String authorId;
//    private String authorName;
//
//    private String content;
//
//    // 대댓글 지원
//    private String parentCommentId; // null이면 원댓글, 값이 있으면 대댓글
//    private Integer depth; // 0: 원댓글, 1: 대댓글
//
//    // 상호작용
//    private Integer likeCount;
//
//    // 상태
//    private boolean active;
//
//    // 타임스탬프
//    private LocalDateTime createdAt;
//    private LocalDateTime updatedAt;
//
//    public static Comment create(String postId, String authorId, String authorName, String content) {
//        return Comment.builder()
//                .postId(postId)
//                .authorId(authorId)
//                .authorName(authorName)
//                .content(content)
//                .depth(0)
//                .likeCount(0)
//                .active(true)
//                .createdAt(LocalDateTime.now())
//                .updatedAt(LocalDateTime.now())
//                .build();
//    }
//
//    public static Comment createReply(String postId, String authorId, String authorName, String content, String parentCommentId) {
//        return Comment.builder()
//                .postId(postId)
//                .authorId(authorId)
//                .authorName(authorName)
//                .content(content)
//                .parentCommentId(parentCommentId)
//                .depth(1)
//                .likeCount(0)
//                .active(true)
//                .createdAt(LocalDateTime.now())
//                .updatedAt(LocalDateTime.now())
//                .build();
//    }
//
//    public void update(String content) {
//        this.content = content;
//        this.updatedAt = LocalDateTime.now();
//    }
//
//    public void incrementLikeCount() {
//        this.likeCount++;
//    }
//
//    public void decrementLikeCount() {
//        if (this.likeCount > 0) {
//            this.likeCount--;
//        }
//    }
//
//    public void delete() {
//        this.active = false;
//        this.updatedAt = LocalDateTime.now();
//    }
//}
//
