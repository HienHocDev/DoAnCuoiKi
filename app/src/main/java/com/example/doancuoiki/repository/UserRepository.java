package com.example.doancuoiki.repository;

import com.example.doancuoiki.model.User;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UserRepository {
    public interface UserCallback {
        void onSuccess(User user);
        void onError(Exception exception);
    }

    public interface SimpleCallback {
        void onSuccess();
        void onError(Exception exception);
    }

    public interface UserListCallback {
        void onSuccess(List<User> users);
        void onError(Exception exception);
    }

    public interface MemberResolveCallback {
        void onSuccess(List<String> userIds, List<String> unresolvedMembers);
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

    public void findUser(String emailOrUid, UserCallback callback) {
        String value = emailOrUid == null ? "" : emailOrUid.trim();
        if (value.isEmpty()) {
            callback.onError(new IllegalArgumentException("Vui lòng nhập email hoặc UID"));
            return;
        }

        if (value.contains("@")) {
            db.collection(COLLECTION_USERS)
                    .whereEqualTo("email", value)
                    .limit(1)
                    .get()
                    .addOnSuccessListener(snapshot -> {
                        if (snapshot.isEmpty()) {
                            callback.onError(new IllegalStateException("Không tìm thấy tài khoản"));
                            return;
                        }
                        DocumentSnapshot document = snapshot.getDocuments().get(0);
                        User user = document.toObject(User.class);
                        if (user == null) {
                            callback.onError(new IllegalStateException("Không tìm thấy tài khoản"));
                            return;
                        }
                        user.setId(document.getId());
                        callback.onSuccess(user);
                    })
                    .addOnFailureListener(callback::onError);
            return;
        }

        getUser(value, callback);
    }

    // NÂNG CẤP CHỐNG LỖI NOT_FOUND: Đổi sang dùng .set và SetOptions.merge()
    public void updateName(String userId, String name, SimpleCallback callback) {
        Map<String, Object> data = new HashMap<>();
        data.put("name", name);

        db.collection(COLLECTION_USERS)
                .document(userId)
                .set(data, SetOptions.merge())
                .addOnSuccessListener(unused -> callback.onSuccess())
                .addOnFailureListener(callback::onError);
    }

    // TÍNH NĂNG CAO CẤP: Lưu link ảnh đại diện (avatarUrl) lên Cloud Firestore
    public void updateAvatar(String uid, String avatarUrl, SimpleCallback callback) {
        Map<String, Object> data = new HashMap<>();
        data.put("avatarUrl", avatarUrl);

        db.collection(COLLECTION_USERS)
                .document(uid)
                .set(data, SetOptions.merge())
                .addOnSuccessListener(unused -> callback.onSuccess())
                .addOnFailureListener(callback::onError);
    }

    public void getUsersByIds(List<String> userIds, UserListCallback callback) {
        if (userIds == null || userIds.isEmpty()) {
            callback.onSuccess(new ArrayList<>());
            return;
        }

        List<com.google.android.gms.tasks.Task<DocumentSnapshot>> requests = new ArrayList<>();
        for (String userId : userIds) {
            if (userId != null && !userId.trim().isEmpty()) {
                requests.add(db.collection(COLLECTION_USERS).document(userId.trim()).get());
            }
        }

        Tasks.whenAllSuccess(requests)
                .addOnSuccessListener(results -> {
                    List<User> users = new ArrayList<>();
                    for (Object result : results) {
                        DocumentSnapshot document = (DocumentSnapshot) result;
                        User user = document.toObject(User.class);
                        if (user != null) {
                            user.setId(document.getId());
                            users.add(user);
                        }
                    }
                    callback.onSuccess(users);
                })
                .addOnFailureListener(callback::onError);
    }

    public void resolveMemberIds(List<String> memberInputs, String ownerId, MemberResolveCallback callback) {
        List<String> uniqueInputs = new ArrayList<>();
        if (memberInputs != null) {
            for (String input : memberInputs) {
                String value = input == null ? "" : input.trim();
                if (!value.isEmpty() && !uniqueInputs.contains(value)) {
                    uniqueInputs.add(value);
                }
            }
        }

        if (uniqueInputs.isEmpty()) {
            List<String> ownerOnly = new ArrayList<>();
            ownerOnly.add(ownerId);
            callback.onSuccess(ownerOnly, new ArrayList<>());
            return;
        }

        List<com.google.android.gms.tasks.Task<?>> requests = new ArrayList<>();
        for (String input : uniqueInputs) {
            if (input.contains("@")) {
                requests.add(db.collection(COLLECTION_USERS)
                        .whereEqualTo("email", input)
                        .limit(1)
                        .get());
            } else {
                requests.add(db.collection(COLLECTION_USERS)
                        .document(input)
                        .get());
            }
        }

        Tasks.whenAllSuccess(requests)
                .addOnSuccessListener(results -> {
                    List<String> userIds = new ArrayList<>();
                    List<String> unresolved = new ArrayList<>();
                    if (ownerId != null && !ownerId.trim().isEmpty()) {
                        userIds.add(ownerId);
                    }

                    for (int i = 0; i < results.size(); i++) {
                        Object result = results.get(i);
                        String input = uniqueInputs.get(i);
                        String resolvedId = "";

                        if (result instanceof DocumentSnapshot) {
                            DocumentSnapshot document = (DocumentSnapshot) result;
                            if (document.exists()) {
                                resolvedId = document.getId();
                            } else if (!input.contains("@")) {
                                resolvedId = input;
                            }
                        } else if (result instanceof com.google.firebase.firestore.QuerySnapshot) {
                            com.google.firebase.firestore.QuerySnapshot snapshot =
                                    (com.google.firebase.firestore.QuerySnapshot) result;
                            if (!snapshot.isEmpty()) {
                                resolvedId = snapshot.getDocuments().get(0).getId();
                            }
                        }

                        if (resolvedId.isEmpty()) {
                            unresolved.add(input);
                        } else if (!userIds.contains(resolvedId)) {
                            userIds.add(resolvedId);
                        }
                    }

                    callback.onSuccess(userIds, unresolved);
                })
                .addOnFailureListener(callback::onError);
    }
}
