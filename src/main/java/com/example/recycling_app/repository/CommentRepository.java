package com.example.recycling_app.repository;

import com.example.recycling_app.domain.Comment;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.Query;
import com.google.cloud.firestore.QuerySnapshot;
import com.google.firebase.cloud.FirestoreClient;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class CommentRepository {
    private final Firestore firestore = FirestoreClient.getFirestore();

    public void save(Comment comment) throws Exception {
        String id = comment.getCommentId() != null ? comment.getCommentId() : UUID.randomUUID().toString();
        comment.setCommentId(id);
        firestore.collection("comments").document(id).set(comment).get();
    }

    public Optional<Comment> findById(String commentId) throws Exception {
        DocumentSnapshot doc = firestore.collection("comments").document(commentId).get().get();
        if(doc.exists()) {
            Comment comment = doc.toObject(Comment.class);
            if(comment !=null && !comment.isDeleted()) {
                return Optional.of(comment);
            }
        }
        return Optional.empty();
    }

    public List<Comment> findByPostIdAndParent(String postId, String parentId) throws Exception {
        Query query = firestore.collection("comments")
                .whereEqualTo("postId", postId)
                .whereEqualTo("deleted", false);
        if (parentId == null) {
            query = query.whereEqualTo("parentId", null); // 1차 댓글 조회
        } else {
            query = query.whereEqualTo("parentId", parentId); // 대댓글 조회
        }
        QuerySnapshot qs = query.get().get();
        List<Comment> result = new ArrayList<>();
        for (DocumentSnapshot doc : qs.getDocuments()) {
            result.add(doc.toObject(Comment.class));
        }
        return result;
    }
}
