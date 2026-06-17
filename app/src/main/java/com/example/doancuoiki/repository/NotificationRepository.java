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
}
