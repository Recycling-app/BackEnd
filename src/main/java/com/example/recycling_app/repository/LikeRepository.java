package com.example.recycling_app.repository;

import com.example.recycling_app.domain.Like;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.Firestore;
import com.google.firebase.cloud.FirestoreClient;
import org.springframework.stereotype.Repository;

import java.util.Date;

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
}
