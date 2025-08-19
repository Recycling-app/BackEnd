package com.example.recycling_app.service;

import com.example.recycling_app.domain.Comment;
import com.example.recycling_app.domain.Post;
import com.example.recycling_app.exception.NotFoundException;
import com.example.recycling_app.exception.UnauthorizedException;
import com.example.recycling_app.repository.CommentRepository;
import com.example.recycling_app.repository.PostRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

@Service
public class CommunityService {

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private CommentRepository commentRepository;

    // 게시글 작성
    public void writePost(Post post) throws Exception {
        post.setCreatedAt(new Date());
        post.setUpdatedAt(new Date());
        post.setDeleted(false); // soft delete 초기화
        if (post.getTitle() == null || post.getTitle().isEmpty()) {
            throw new IllegalArgumentException("게시글 제목은 필수입니다.");
        }
        postRepository.save(post);
    }

    // 전체 게시글 조회
    public List<Post> getAllPosts() throws Exception {
        return postRepository.findAll();
    }

    // 카테고리 별 게시글 리스트 조회 (Soft Delete 적용)
    public List<Post> getPostsByCategory(String category) throws Exception {
        return postRepository.findByCategory(category);
    }

    // 단일 게시글 조회 (Soft Delete 적용)
    public Post getPost(String postId) throws Exception {
        return postRepository.findById(postId)
                .orElseThrow(() -> new NotFoundException("게시글이 존재하지 않습니다."));
    }

    // 게시글 수정
    public void updatePost(String postId, String uid, Post updatedPost) throws Exception {
        Post existingPost = postRepository.findById(postId)
                .orElseThrow(() -> new NotFoundException("게시글이 존재하지 않습니다."));

        if (!existingPost.getUid().equals(uid)) {
            throw new UnauthorizedException("수정 권한이 없습니다.");
        }
        if (existingPost.isDeleted()) {
            throw new NotFoundException("삭제된 글은 수정할 수 없습니다.");
        }

        existingPost.setTitle(updatedPost.getTitle());
        existingPost.setCategory(updatedPost.getCategory());
        existingPost.setContents(updatedPost.getContents());
        existingPost.setUpdatedAt(new Date());

        postRepository.save(existingPost);
    }

    // 게시글 논리 삭제
    public void deletePost(String postId, String uid) throws Exception {
        Post existingPost = postRepository.findById(postId)
                .orElseThrow(() -> new NotFoundException("게시글이 존재하지 않습니다."));

        if (!existingPost.getUid().equals(uid)) {
            throw new UnauthorizedException("삭제 권한이 없습니다.");
        }

        existingPost.setDeleted(true);
        existingPost.setDeletedAt(new Date());
        postRepository.save(existingPost);
    }

    // 댓글 작성
    public void writeComment(Comment comment) throws Exception {
        comment.setCreatedAt(new Date());
        comment.setUpdatedAt(new Date());
        comment.setDeleted(false);  // soft delete 초기화

        // 최상위 댓글인 경우 명시적 null 처리 필수
        if (comment.getParentId() == null) {
            comment.setParentId(null);
        }

        commentRepository.save(comment);
    }

    // 게시글별 댓글 및 대댓글 조회
    public List<Comment> getCommentsByPostIdAndParent(String postId, String parentId) throws Exception {
        if (parentId == null)
            return commentRepository.findByPostIdAndParent(postId, null);
        else
            return commentRepository.findByPostIdAndParent(postId, parentId);
    }

    // 단일 댓글 조회 (Soft Delete 적용)
    public Comment getComment(String commentId) throws Exception {
        return commentRepository.findById(commentId)
                .orElseThrow(() -> new NotFoundException("댓글이 존재하지 않습니다."));
    }

    // 댓글 수정
    public void updateComment(String commentId, String uid, String content) throws Exception {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new NotFoundException("댓글이 존재하지 않습니다."));

        if (!comment.getUid().equals(uid)) {
            throw new UnauthorizedException("수정 권한이 없습니다.");
        }
        if (comment.isDeleted()) {
            throw new NotFoundException("삭제된 댓글은 수정할 수 없습니다.");
        }

        comment.setContent(content);
        comment.setUpdatedAt(new Date());
        commentRepository.save(comment);
    }

    // 댓글 논리 삭제
    public void deleteComment(String commentId, String uid) throws Exception {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new NotFoundException("댓글이 존재하지 않습니다."));

        if (!comment.getUid().equals(uid)) {
            throw new UnauthorizedException("삭제 권한이 없습니다.");
        }

        comment.setDeleted(true);
        comment.setDeletedAt(new Date());
        commentRepository.save(comment);
    }

    public int incrementLikes(String postId, int increment) throws Exception {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new NotFoundException("게시글이 존재하지 않습니다."));
        post.setLikesCount(post.getLikesCount() + increment);
        postRepository.save(post);
        return post.getLikesCount();
    }
}