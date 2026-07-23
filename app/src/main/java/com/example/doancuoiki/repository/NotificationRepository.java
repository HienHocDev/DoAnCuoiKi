package com.example.doancuoiki.repository;

import com.example.doancuoiki.model.NotificationItem;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class NotificationRepository {
    public interface NotificationListCallback {
        void onSuccess(List<NotificationItem> notifications);

        void onError(Exception exception);
    }

    private static final String COLLECTION_NOTIFICATIONS = "notifications";

    private final FirebaseFirestore db = FirebaseFirestore.getInstance();

    public void getNotificationsByUser(String userId, NotificationListCallback callback) {
        db.collection(COLLECTION_NOTIFICATIONS)
                .whereEqualTo("userId", userId)
                .get()
                .addOnSuccessListener(snapshot -> {
                    List<NotificationItem> notifications = new ArrayList<>();
                    for (DocumentSnapshot document : snapshot.getDocuments()) {
                        NotificationItem notification = document.toObject(NotificationItem.class);
                        if (notification != null) {
                            notification.setId(document.getId());
                            notifications.add(notification);
                        }
                    }
                    callback.onSuccess(notifications);
                })
                .addOnFailureListener(callback::onError);
    }
    public interface SimpleCallback {
        void onSuccess();
        void onError(Exception exception);
    }

    public interface CountCallback {
        void onCount(int count);
    }

    public void addNotification(NotificationItem notification, SimpleCallback callback) {
        com.google.firebase.firestore.DocumentReference document = db.collection(COLLECTION_NOTIFICATIONS).document();
        notification.setId(document.getId());

        String today = new java.text.SimpleDateFormat("dd/MM/yyyy HH:mm", java.util.Locale.getDefault()).format(new java.util.Date());
        if (notification.getCreatedAt() == null || notification.getCreatedAt().isEmpty()) {
            notification.setCreatedAt(today);
        }

        document.set(notification)
                .addOnSuccessListener(unused -> {
                    if (callback != null) callback.onSuccess();
                })
                .addOnFailureListener(e -> {
                    if (callback != null) callback.onError(e);
                });
    }

    public void markAllRead(String userId, SimpleCallback callback) {
        db.collection(COLLECTION_NOTIFICATIONS)
                .whereEqualTo("userId", userId)
                .whereEqualTo("read", false)
                .get()
                .addOnSuccessListener(snapshot -> {
                    com.google.firebase.firestore.WriteBatch batch = db.batch();
                    for (DocumentSnapshot doc : snapshot.getDocuments()) {
                        batch.update(doc.getReference(), "read", true);
                    }
                    batch.commit()
                            .addOnSuccessListener(unused -> { if (callback != null) callback.onSuccess(); })
                            .addOnFailureListener(e -> { if (callback != null) callback.onError(e); });
                })
                .addOnFailureListener(e -> { if (callback != null) callback.onError(e); });
    }

    public void deleteNotification(String notificationId, SimpleCallback callback) {
        db.collection(COLLECTION_NOTIFICATIONS)
                .document(notificationId)
                .delete()
                .addOnSuccessListener(unused -> { if (callback != null) callback.onSuccess(); })
                .addOnFailureListener(e -> { if (callback != null) callback.onError(e); });
    }

    public void getUnreadCount(String userId, CountCallback callback) {
        db.collection(COLLECTION_NOTIFICATIONS)
                .whereEqualTo("userId", userId)
                .whereEqualTo("read", false)
                .get()
                .addOnSuccessListener(snapshot -> {
                    if (callback != null) callback.onCount(snapshot.size());
                })
                .addOnFailureListener(e -> {
                    if (callback != null) callback.onCount(0);
                });
    }
}
