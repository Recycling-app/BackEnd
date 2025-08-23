package com.example.recycling_app.repository;

import com.example.recycling_app.domain.Post;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.QuerySnapshot;
import com.google.firebase.cloud.FirestoreClient;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
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

    public Optional<Post> findById(String postId) throws Exception {
        DocumentSnapshot doc = firestore.collection("posts").document(postId).get().get();
        System.out.println("Firestore에서 받은 doc.exists: " + doc.exists() + ", doc.id: " + doc.getId());
        if(doc.exists()) {
            Post post = doc.toObject(Post.class);
            System.out.println("Post.isDeleted: " + (post != null ? post.isDeleted() : "post is null"));
            if(post!= null && !post.isDeleted()){
                return Optional.of(post);
            }
        }
        return Optional.empty();
    }

    // 전체 게시글 조회
    public List<Post> findAll() throws Exception {
        QuerySnapshot qs = firestore.collection("posts")
                .whereEqualTo("deleted", false)
                .get().get();
        List<Post> result = new ArrayList<>();
        for (DocumentSnapshot doc : qs.getDocuments()) {
            result.add(doc.toObject(Post.class));
        }
        return result;
    }

    // 카테고리별 조회
    public List<Post> findByCategory(String category) throws Exception {
        QuerySnapshot qs = firestore.collection("posts")
                .whereEqualTo("category", category)
                .whereEqualTo("deleted", false)
                .get().get();
        List<Post> result = new ArrayList<>();
        for (DocumentSnapshot doc : qs.getDocuments()) {
            result.add(doc.toObject(Post.class));
        }
        return result;
    }

    // UID 기준 작성 게시글 조회 (deleted = false)
    public List<Post> findByUidAndDeletedFalse(String uid) throws Exception {
        QuerySnapshot qs = firestore.collection("posts")
                .whereEqualTo("uid", uid)
                .whereEqualTo("deleted", false)
                .get().get();

        List<Post> result = new ArrayList<>();
        for (DocumentSnapshot doc : qs.getDocuments()) {
            result.add(doc.toObject(Post.class));
        }
        return result;
    }

    // ID 리스트로 게시글 조회(중복 제거, deleted 검사 없음)
    public List<Post> findAllById(List<String> postIds) throws Exception {
        List<Post> result = new ArrayList<>();
        for (String postId : postIds) {
            DocumentSnapshot doc = firestore.collection("posts").document(postId).get().get();
            if (doc.exists()) {
                Post post = doc.toObject(Post.class);
                if (post != null && !post.isDeleted()) {
                    result.add(post);
                }
            }
        }
        return result;
    }
}
