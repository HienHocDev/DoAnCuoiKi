package com.example.doancuoiki.repository;

import com.example.doancuoiki.model.User;
import com.google.firebase.firestore.FirebaseFirestore;

public class UserRepository {
    public interface UserCallback {
        void onSuccess(User user);

        void onError(Exception exception);
    }

    public interface SimpleCallback {
        void onSuccess();

        void onError(Exception exception);
    }

    private static final String COLLECTION_USERS = "users";

    private final FirebaseFirestore db = FirebaseFirestore.getInstance();

    public void createUser(User user, SimpleCallback callback) {
        db.collection(COLLECTION_USERS)
                .document(user.getId())
                .set(user)
                .addOnSuccessListener(unused -> callback.onSuccess())
                .addOnFailureListener(callback::onError);
    }

    public void getUser(String userId, UserCallback callback) {
        db.collection(COLLECTION_USERS)
                .document(userId)
                .get()
                .addOnSuccessListener(document -> {
                    User user = document.toObject(User.class);
                    if (user == null) {
                        callback.onError(new IllegalStateException("Không tìm thấy thông tin người dùng"));
                        return;
                    }
                    user.setId(document.getId());
                    callback.onSuccess(user);
                })
                .addOnFailureListener(callback::onError);
    }

    public void updateName(String userId, String name, SimpleCallback callback) {
        db.collection(COLLECTION_USERS)
                .document(userId)
                .update("name", name)
                .addOnSuccessListener(unused -> callback.onSuccess())
                .addOnFailureListener(callback::onError);
    }
}
