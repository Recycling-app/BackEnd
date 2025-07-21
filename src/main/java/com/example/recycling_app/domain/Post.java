//package com.example.recycling_app.domain;
//
//import lombok.*;
//import org.springframework.data.annotation.Id;
//
//import java.time.LocalDateTime;
//import java.util.List;
//
//@Data
//@NoArgsConstructor
//@AllArgsConstructor
//@Builder
//public class Post {
//
//    @Id
//    private String id;
//
//    private String authorId;
//    private String authorName;
//
//    private String title;
//    private String content;
//    private String category; // "분리수거", "환경보호", "업사이클링", "자유"
//
//    // 첨부 파일 및 이미지
//    private List<String> imageUrls;
//    private List<String> attachmentUrls;
//
//    // 상호작용 관련
//    private Integer viewCount;
//    private Integer likeCount;
//    private Integer commentCount;
//
//    // 상태
//    private boolean active; // 삭제된 게시글 처리
//    private boolean pinned; // 공지사항 등 상단 고정
//
//    // 타임스탬프
//    private LocalDateTime createdAt;
//    private LocalDateTime updatedAt;
//
//    public static Post create(String authorId, String authorName, String title, String content, String category) {
//        return Post.builder()
//                .authorId(authorId)
//                .authorName(authorName)
//                .title(title)
//                .content(content)
//                .category(category)
//                .viewCount(0)
//                .likeCount(0)
//                .commentCount(0)
//                .active(true)
//                .pinned(false)
//                .createdAt(LocalDateTime.now())
//                .updatedAt(LocalDateTime.now())
//                .build();
//    }
//
//    public void update(String title, String content, String category) {
//        this.title = title;
//        this.content = content;
//        this.category = category;
//        this.updatedAt = LocalDateTime.now();
//    }
//
//    public void incrementViewCount() {
//        this.viewCount++;
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
//    public void incrementCommentCount() {
//        this.commentCount++;
//    }
//
//    public void decrementCommentCount() {
//        if (this.commentCount > 0) {
//            this.commentCount--;
//        }
//    }
//
//    public void delete() {
//        this.active = false;
//        this.updatedAt = LocalDateTime.now();
//    }
//}

