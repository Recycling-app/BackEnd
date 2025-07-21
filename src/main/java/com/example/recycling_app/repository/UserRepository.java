package com.example.recycling_app.repository;

import com.example.recycling_app.domain.User;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.concurrent.ExecutionException;

@Repository
@RequiredArgsConstructor
public class UserRepository {
    private final Firestore firestore;
    private static final String COLLECTION = "users";

    public void save(User user) throws ExecutionException, InterruptedException {
        firestore.collection(COLLECTION).document(user.getUid()).set(user).get();
    }

    public boolean existsByUid(String uid) throws ExecutionException, InterruptedException {
        DocumentSnapshot snapshot = firestore.collection(COLLECTION).document(uid).get().get();
        return snapshot.exists();
    }
}

