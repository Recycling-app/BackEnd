package com.example.recycling_app.controller;

import com.example.recycling_app.domain.Comment;
import com.example.recycling_app.domain.Post;
import com.example.recycling_app.service.CommunityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/community")
public class CommunityController {
    @Autowired
    private CommunityService communityService;

    // 게시글 작성
    @PostMapping("/posts")
    public ResponseEntity<?> writePost(@RequestBody Post post) throws Exception {
        communityService.writePost(post);
        return ResponseEntity.ok().build();
    }

    // 게시글 목록 조회 (카테고리별)
    @GetMapping("/posts")
    public List<Post> getPosts(@RequestParam String category) throws Exception {
        return communityService.getPostsByCategory(category);
    }

    // 댓글 작성
    @PostMapping("/posts/{postId}/comments")
    public ResponseEntity<?> addComment(@PathVariable String postId,
                                        @RequestBody Comment comment) throws Exception {
        comment.setPostId(postId);
        communityService.addComment(comment);
        return ResponseEntity.ok().build();
    }

    // 좋아요
    @PostMapping("/posts/{postId}/like")
    public ResponseEntity<?> addLike(@PathVariable String postId,
                                     @RequestParam String uid) throws Exception {
        communityService.addLike(postId, uid);
        return ResponseEntity.ok().build();
    }
}
