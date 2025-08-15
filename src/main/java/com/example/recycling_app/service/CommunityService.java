package com.example.recycling_app.service;

import com.example.recycling_app.domain.Comment;
import com.example.recycling_app.domain.Post;
import com.example.recycling_app.repository.CommentRepository;
import com.example.recycling_app.repository.LikeRepository;
import com.example.recycling_app.repository.PostRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CommunityService {
    @Autowired
    private PostRepository postRepository;
    @Autowired
    private CommentRepository commentRepository;
    @Autowired
    private LikeRepository likeRepository;

    // 게시글 작성
    public void writePost(Post post) throws Exception {
        postRepository.save(post);
    }

    // 카테고리별 게시글 조회
    public List<Post> getPostsByCategory(String category) throws Exception {
        return postRepository.findByCategory(category);
    }

    // 댓글 작성
    public void addComment(Comment comment) throws Exception {
        commentRepository.save(comment);
    }

    // 좋아요 추가(중복 방지)
    public void addLike(String postId, String uid) throws Exception {
        likeRepository.save(postId, uid);
        // likesCount 증가는 별도 비동기/트랜잭션 처리 추천
    }
}
