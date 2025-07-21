package com.example.recycling_app.repository;

import com.example.recycling_app.domain.User;
import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.Query;
import com.google.cloud.firestore.QuerySnapshot;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class UserRepository {

    private final Firestore firestore;
    private static final String COLLECTION = "users";

    public void save(User user) throws Exception {
        firestore.collection(COLLECTION).document(user.getUid()).set(user).get();
    }

    public boolean existsByUid(String uid) throws Exception {
        DocumentSnapshot snapshot = firestore.collection(COLLECTION).document(uid).get().get();
        return snapshot.exists();
    }

    public Optional<User> findByEmail(String email) throws Exception {
        Query query = firestore.collection(COLLECTION).whereEqualTo("email", email);
        ApiFuture<QuerySnapshot> future = query.get();
        QuerySnapshot snapshot = future.get();
        if (!snapshot.isEmpty()) {
            DocumentSnapshot doc = snapshot.getDocuments().get(0);
            return Optional.of(doc.toObject(User.class));
        }
        return Optional.empty();
    }
}

