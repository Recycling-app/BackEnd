package com.example.recycling_app.repository;

import com.example.recycling_app.domain.Like;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.QuerySnapshot;
import com.google.firebase.cloud.FirestoreClient;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@Repository
public class LikeRepository {
    private final Firestore firestore = FirestoreClient.getFirestore();

    public void save(String postId, String uid) throws Exception {
        String likeId = postId + "_" + uid;
        DocumentReference likeRef = firestore.collection("likes").document(likeId);
        if (likeRef.get().get().exists())
            throw new IllegalStateException("이미 좋아요를 눌렀습니다.");
        Like like = Like.builder()
                .likeId(likeId)
                .postId(postId)
                .uid(uid)
                .likedAt(new Date())
                .build();
        likeRef.set(like).get();
    }

    // 좋아요 존재 여부를 확인하는 메서드 추가
    public Optional<Like> findById(String postId, String uid) throws Exception {
        String likeId = postId + "_" + uid;
        DocumentSnapshot doc = firestore.collection("likes").document(likeId).get().get();
        if (doc.exists()) {
            return Optional.ofNullable(doc.toObject(Like.class));
        }
        return Optional.empty();
    }

    // 좋아요를 삭제하는 메서드 추가
    public void deleteById(String postId, String uid) throws Exception {
        String likeId = postId + "_" + uid;
        firestore.collection("likes").document(likeId).delete().get();
    }

    // 내가 좋아요한 게시글 목록 조회
    public List<com.example.recycling_app.domain.Post> findPostsLikedByUser(String uid) throws Exception {
        QuerySnapshot likeQ = firestore.collection("likes")
                .whereEqualTo("uid", uid)
                .get().get();

        List<String> postIds = new ArrayList<>();
        for (DocumentSnapshot likeDoc : likeQ.getDocuments()) {
            String postId = likeDoc.getString("postId");
            if (postId != null && !postIds.contains(postId)) {
                postIds.add(postId);
            }
        }

        List<com.example.recycling_app.domain.Post> posts = new ArrayList<>();
        for (String postId : postIds) {
            DocumentSnapshot postDoc = firestore.collection("posts").document(postId).get().get();
            if (postDoc.exists()) {
                com.example.recycling_app.domain.Post post = postDoc.toObject(com.example.recycling_app.domain.Post.class);
                if (post != null && !post.isDeleted()) {
                    posts.add(post);
                }
            }
        }
        return posts;
    }
}
