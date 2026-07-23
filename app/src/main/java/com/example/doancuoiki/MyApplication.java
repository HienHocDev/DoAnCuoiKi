package com.example.doancuoiki;

import android.app.Application;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;

import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.app.NotificationCompat;

import com.example.doancuoiki.model.NotificationItem;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;

public class MyApplication extends Application {

    private static final String CHANNEL_ID = "realtime_notifications";

    private static final String PREF_NAME = "APP_SETTINGS";
    private static final String KEY_NOTIFICATIONS = "notifications_enabled";
    private static final String KEY_DARK_MODE = "dark_mode_enabled";

    private ListenerRegistration notificationListener;
    private boolean isFirstLoad = true;

    @Override
    public void onCreate() {
        super.onCreate();

        // Đọc và áp dụng chế độ tối đã lưu
        SharedPreferences preferences =
                getSharedPreferences(PREF_NAME, MODE_PRIVATE);

        boolean darkModeEnabled =
                preferences.getBoolean(KEY_DARK_MODE, false);

        if (darkModeEnabled) {
            AppCompatDelegate.setDefaultNightMode(
                    AppCompatDelegate.MODE_NIGHT_YES
            );
        } else {
            AppCompatDelegate.setDefaultNightMode(
                    AppCompatDelegate.MODE_NIGHT_NO
            );
        }

        createNotificationChannel();

        FirebaseAuth.getInstance().addAuthStateListener(firebaseAuth -> {
            if (firebaseAuth.getCurrentUser() != null) {
                startListeningForNotifications(
                        firebaseAuth.getCurrentUser().getUid()
                );
            } else {
                stopListeningForNotifications();
            }
        });
    }

    private void startListeningForNotifications(String userId) {
        if (notificationListener != null) {
            return;
        }

        isFirstLoad = true;

        notificationListener = FirebaseFirestore.getInstance()
                .collection("notifications")
                .whereEqualTo("userId", userId)
                .addSnapshotListener((snapshots, error) -> {
                    if (error != null || snapshots == null) {
                        return;
                    }

                    if (isFirstLoad) {
                        isFirstLoad = false;
                        return;
                    }

                    for (DocumentChange dc : snapshots.getDocumentChanges()) {
                        if (dc.getType() == DocumentChange.Type.ADDED) {
                            NotificationItem item =
                                    dc.getDocument()
                                            .toObject(NotificationItem.class);

                            showSystemNotification(item);
                        }
                    }
                });
    }

    private void stopListeningForNotifications() {
        if (notificationListener != null) {
            notificationListener.remove();
            notificationListener = null;
        }
    }

    private void showSystemNotification(NotificationItem item) {

        // Kiểm tra người dùng có đang bật thông báo không
        boolean notificationsEnabled =
                getSharedPreferences(PREF_NAME, MODE_PRIVATE)
                        .getBoolean(KEY_NOTIFICATIONS, true);

        if (!notificationsEnabled) {
            return;
        }

        NotificationManager notificationManager =
                (NotificationManager)
                        getSystemService(Context.NOTIFICATION_SERVICE);

        if (notificationManager == null) {
            return;
        }

        Intent intent;

        if (item.getTaskId() != null
                && !item.getTaskId().isEmpty()) {

            intent = new Intent(
                    this,
                    TaskDetailActivity.class
            );

            intent.putExtra(
                    TaskDetailActivity.EXTRA_TASK_ID,
                    item.getTaskId()
            );

        } else {
            intent = new Intent(
                    this,
                    NotificationsActivity.class
            );
        }

        intent.setFlags(
                Intent.FLAG_ACTIVITY_NEW_TASK
                        | Intent.FLAG_ACTIVITY_CLEAR_TASK
        );

        int notificationId =
                item.getId() != null
                        ? item.getId().hashCode()
                        : (int) System.currentTimeMillis();

        PendingIntent pendingIntent =
                PendingIntent.getActivity(
                        this,
                        notificationId,
                        intent,
                        PendingIntent.FLAG_UPDATE_CURRENT
                                | PendingIntent.FLAG_IMMUTABLE
                );

        String title =
                item.getTitle() != null
                        ? item.getTitle()
                        : "Thông báo mới";

        String message =
                item.getMessage() != null
                        ? item.getMessage()
                        : "Bạn có một thông báo mới";

        NotificationCompat.Builder builder =
                new NotificationCompat.Builder(
                        this,
                        CHANNEL_ID
                )
                        .setSmallIcon(
                                android.R.drawable.ic_popup_reminder
                        )
                        .setContentTitle(title)
                        .setContentText(message)
                        .setPriority(
                                NotificationCompat.PRIORITY_HIGH
                        )
                        .setDefaults(
                                NotificationCompat.DEFAULT_ALL
                        )
                        .setAutoCancel(true)
                        .setContentIntent(pendingIntent);

        notificationManager.notify(
                notificationId,
                builder.build()
        );
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT
                >= Build.VERSION_CODES.O) {

            NotificationChannel channel =
                    new NotificationChannel(
                            CHANNEL_ID,
                            "Thông báo trong ứng dụng",
                            NotificationManager.IMPORTANCE_HIGH
                    );

            channel.setDescription(
                    "Kênh thông báo các hoạt động và phân công công việc"
            );

            NotificationManager manager =
                    getSystemService(
                            NotificationManager.class
                    );

            if (manager != null) {
                manager.createNotificationChannel(channel);
            }
        }
    }
}