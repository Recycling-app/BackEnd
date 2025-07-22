package com.example.recycling_app.repository;

import com.example.recycling_app.domain.User;
import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.QuerySnapshot;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class UserRepository {

    private final Firestore firestore;
    private final String COLLECTION = "users";

    public void save(User user) throws Exception {
        firestore.collection(COLLECTION).document(user.getUid()).set(user).get();
    }

    public Optional<User> findByEmail(String email) throws Exception {
        ApiFuture<QuerySnapshot> future = firestore.collection(COLLECTION)
                .whereEqualTo("email", email)
                .get();
        QuerySnapshot snapshot = future.get();
        if (!snapshot.isEmpty()) {
            User user = snapshot.getDocuments().get(0).toObject(User.class);
            return Optional.of(user);
        }
        return Optional.empty();
    }

    public boolean existsByUid(String uid) throws Exception {
        DocumentSnapshot snapshot = firestore.collection(COLLECTION)
                .document(uid)
                .get()
                .get();
        return snapshot.exists();
    }
}
