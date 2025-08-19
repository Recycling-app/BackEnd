package com.example.recycling_app.controller;

import com.example.recycling_app.domain.Comment;
import com.example.recycling_app.domain.Post;
import com.example.recycling_app.service.CommunityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/community")
public class CommunityController {

    @Autowired
    private CommunityService communityService;

    // 게시글 작성
    @PostMapping("/posts")
    public ResponseEntity<Map<String, String>> createPost(@RequestBody Post post) throws Exception {
        communityService.writePost(post);
        return ResponseEntity.status(201).body(Map.of("message", "게시글이 성공적으로 작성되었습니다."));
    }

    // 전체 게시글 조회
    @GetMapping("/posts/all")
    public ResponseEntity<List<Post>> getAllPosts() throws Exception {
        List<Post> posts = communityService.getAllPosts();
        return ResponseEntity.ok(posts);
    }

    // 카테고리별 게시글 조회
    @GetMapping("/posts")
    public ResponseEntity<List<Post>> getPostsByCategory(@RequestParam String category) throws Exception {
        List<Post> posts = communityService.getPostsByCategory(category);
        return ResponseEntity.ok(posts);
    }

    // 게시글 수정
    @PutMapping("/posts/{postId}")
    public ResponseEntity<Map<String, String>> updatePost(@PathVariable String postId,
                                                          @RequestParam String uid,
                                                          @RequestBody Post updatedPost) throws Exception {
        communityService.updatePost(postId, uid, updatedPost);
        return ResponseEntity.ok(Map.of("message", "게시글이 성공적으로 수정되었습니다."));
    }

    // 게시글 삭제(논리삭제)
    @DeleteMapping("/posts/{postId}")
    public ResponseEntity<Void> deletePost(@PathVariable String postId,
                                           @RequestParam String uid) throws Exception {
        communityService.deletePost(postId, uid);
        return ResponseEntity.noContent().build(); // 204 No Content
    }

    // 댓글 작성
    @PostMapping("/posts/{postId}/comments")
    public ResponseEntity<?> writeComment(@PathVariable String postId, @RequestBody Comment comment) throws Exception {
        comment.setPostId(postId);

        // 부모댓글 ID가 있으면 parentId 필드에 정확히 설정 (null 가능)
        if (comment.getParentId() != null && !comment.getParentId().isEmpty()) {
            // parentId는 직접 받은 부모 댓글 commentId
        } else {
            comment.setParentId(null); // 일반 댓글일 때 null로 명시
        }

        communityService.writeComment(comment);
        return ResponseEntity.status(201).body("댓글이 성공적으로 작성되었습니다.");
    }


    // 댓글 및 대댓글 조회 (parentId optional)
    @GetMapping("/posts/{postId}/comments")
    public ResponseEntity<List<Comment>> getComments(@PathVariable String postId,
                                                     @RequestParam(required = false) String parentId) throws Exception {
        List<Comment> comments = communityService.getCommentsByPostIdAndParent(postId, parentId);
        return ResponseEntity.ok(comments);
    }

    // 댓글 수정
    @PutMapping("/comments/{commentId}")
    public ResponseEntity<Map<String, String>> updateComment(@PathVariable String commentId,
                                                             @RequestParam String uid,
                                                             @RequestBody Map<String, String> body) throws Exception {
        String content = body.get("content");
        communityService.updateComment(commentId, uid, content);
        return ResponseEntity.ok(Map.of("message", "댓글이 성공적으로 수정되었습니다."));
    }

    // 댓글 삭제(논리삭제)
    @DeleteMapping("/comments/{commentId}")
    public ResponseEntity<Void> deleteComment(@PathVariable String commentId,
                                              @RequestParam String uid) throws Exception {
        communityService.deleteComment(commentId, uid);
        return ResponseEntity.noContent().build();
    }

    // 게시글 좋아요 수 증가
    @PatchMapping("/posts/{postId}/like")
    public ResponseEntity<?> incrementLikes(@PathVariable String postId, @RequestBody Map<String, Integer> body) throws Exception {
        int increment = body.getOrDefault("increment", 1);
        int likes = communityService.incrementLikes(postId, increment);
        return ResponseEntity.ok(Map.of("likesCount", likes, "message", "좋아요가 정상적으로 반영되었습니다."));
    }
}