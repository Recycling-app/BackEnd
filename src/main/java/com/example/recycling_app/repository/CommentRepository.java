package com.example.recycling_app.repository;

import com.example.recycling_app.domain.Comment;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.QuerySnapshot;
import com.google.firebase.cloud.FirestoreClient;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Repository
public class CommentRepository {
    private final Firestore firestore = FirestoreClient.getFirestore();

    public void save(Comment comment) throws Exception {
        String id = comment.getCommentId() != null ? comment.getCommentId() : UUID.randomUUID().toString();
        comment.setCommentId(id);
        firestore.collection("comments").document(id).set(comment).get();
    }

    public List<Comment> findByPostId(String postId) throws Exception {
        QuerySnapshot qs = firestore.collection("comments")
                .whereEqualTo("postId", postId).get().get();
        List<Comment> result = new ArrayList<>();
        for (DocumentSnapshot doc : qs.getDocuments()) {
            result.add(doc.toObject(Comment.class));
        }
        return result;
    }
}
