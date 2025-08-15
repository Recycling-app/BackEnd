package com.example.recycling_app.repository;

import com.example.recycling_app.domain.Post;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.QuerySnapshot;
import com.google.firebase.cloud.FirestoreClient;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Repository
public class PostRepository {
    private final Firestore firestore = FirestoreClient.getFirestore();

    // 게시글 저장
    public void save(Post post) throws Exception {
        String id = post.getPostId() != null ? post.getPostId() : UUID.randomUUID().toString();
        post.setPostId(id);
        firestore.collection("posts").document(id).set(post).get();
    }

    // 카테고리별 조회
    public List<Post> findByCategory(String category) throws Exception {
        QuerySnapshot qs = firestore.collection("posts")
                .whereEqualTo("category", category).get().get();
        List<Post> result = new ArrayList<>();
        for (DocumentSnapshot doc : qs.getDocuments()) {
            result.add(doc.toObject(Post.class));
        }
        return result;
    }
}
