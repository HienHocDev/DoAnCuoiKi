package com.example.doancuoiki;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.doancuoiki.model.NotificationItem;
import com.example.doancuoiki.model.Task;
import com.example.doancuoiki.repository.NotificationRepository;
import com.example.doancuoiki.repository.TaskRepository;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class NotificationsActivity extends Activity {
    private static final String GUEST_USER_ID = "guest";

    private final NotificationRepository notificationRepository = new NotificationRepository();
    private final TaskRepository taskRepository = new TaskRepository();
    private final List<NotificationItem> notifications = new ArrayList<>();

    private LinearLayout notificationList;
    private TextView notificationState;
    private Button btnMarkAllRead;
    private String currentUserId = GUEST_USER_ID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notifications);

        View mainLayout = findViewById(android.R.id.content);
        if (mainLayout != null) {
            ViewCompat.setOnApplyWindowInsetsListener(mainLayout, (v, windowInsets) -> {
                Insets insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars());
                v.setPadding(v.getPaddingLeft(), insets.top, v.getPaddingRight(), insets.bottom);
                return windowInsets;
            });
        }

        notificationList = findViewById(R.id.notificationList);
        notificationState = findViewById(R.id.txtNotificationState);
        btnMarkAllRead = findViewById(R.id.btnMarkAllRead);
        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        // B7 — Mark all read button
        if (btnMarkAllRead != null) {
            btnMarkAllRead.setOnClickListener(v -> markAllRead());
        }

        resolveCurrentUser();
        loadNotifications();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (notificationList != null) {
            loadNotifications();
        }
    }

    private void resolveCurrentUser() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            currentUserId = user.getUid();
        }
    }

    private void loadNotifications() {
        if (notificationState != null) notificationState.setText("Đang tải thông báo...");
        notifications.clear();

        notificationRepository.getNotificationsByUser(currentUserId, new NotificationRepository.NotificationListCallback() {
            @Override
            public void onSuccess(List<NotificationItem> firestoreNotifications) {
                notifications.addAll(firestoreNotifications);
                deduplicateNotifications();
                renderNotifications();
            }

            @Override
            public void onError(Exception exception) {
                deduplicateNotifications();
                renderNotifications();
            }
        });
    }

    private void deduplicateNotifications() {
        Map<String, NotificationItem> notificationMap = new LinkedHashMap<>();
        for (NotificationItem notification : notifications) {
            String key = notification.getId();
            if (key == null || key.trim().isEmpty()) {
                key = notification.getTitle() + "-" + notification.getMessage();
            }
            notificationMap.put(key, notification);
        }
        notifications.clear();
        notifications.addAll(notificationMap.values());
        java.util.Collections.reverse(notifications);
    }

    private void renderNotifications() {
        notificationList.removeAllViews();

        long unreadCount = notifications.stream().filter(n -> !n.isRead()).count();

        if (notifications.isEmpty()) {
            if (notificationState != null) notificationState.setText("Chưa có thông báo.");
            if (btnMarkAllRead != null) btnMarkAllRead.setVisibility(View.GONE);
            return;
        }

        if (notificationState != null) {
            notificationState.setText("Có " + notifications.size() + " thông báo"
                    + (unreadCount > 0 ? " (" + unreadCount + " chưa đọc)" : " (Đã đọc tất cả)"));
        }
        if (btnMarkAllRead != null) {
            btnMarkAllRead.setVisibility(unreadCount > 0 ? View.VISIBLE : View.GONE);
        }

        for (NotificationItem notification : notifications) {
            View card = ViewFactory.notificationCard(
                    this,
                    notification.getTitle(),
                    notification.getMessage(),
                    notification.isRead() ? "Đã đọc" : notification.getCreatedAt()
            );

            // B7 — Long-press to delete notification
            card.setOnLongClickListener(v -> {
                showDeleteDialog(notification);
                return true;
            });

            if (notification.getTaskId() != null && !notification.getTaskId().isEmpty()) {
                card.setOnClickListener(v -> {
                    markAsRead(notification);
                    openTaskDetail(notification.getTaskId());
                });
            } else if (notification.getId() != null) {
                String id = notification.getId();
                if (id.startsWith("task-")) id = id.replace("task-", "");
                if (id.startsWith("deadline-")) id = id.replace("deadline-", "");
                if (id.startsWith("done-")) id = id.replace("done-", "");
                final String finalId = id;
                card.setOnClickListener(v -> {
                    markAsRead(notification);
                    openTaskDetail(finalId);
                });
            }
            notificationList.addView(card);
        }
    }

    private void markAsRead(NotificationItem notification) {
        if (notification.isRead() || notification.getId() == null) return;
        notification.setRead(true);
        // Update in Firestore
        com.google.firebase.firestore.FirebaseFirestore.getInstance()
                .collection("notifications")
                .document(notification.getId())
                .update("read", true);
    }

    private void markAllRead() {
        notificationRepository.markAllRead(currentUserId, new NotificationRepository.SimpleCallback() {
            @Override
            public void onSuccess() {
                for (NotificationItem n : notifications) {
                    n.setRead(true);
                }
                renderNotifications();
                NavigationUtils.showMessage(NotificationsActivity.this, "Đã đánh dấu đã đọc tất cả");
            }

            @Override
            public void onError(Exception exception) {
                NavigationUtils.showMessage(NotificationsActivity.this, "Có lỗi xảy ra");
            }
        });
    }

    private void showDeleteDialog(NotificationItem notification) {
        new AlertDialog.Builder(this)
                .setTitle("Xóa thông báo")
                .setMessage("Bạn có muốn xóa thông báo này không?")
                .setNegativeButton("Hủy", null)
                .setPositiveButton("Xóa", (dialog, which) -> {
                    notificationRepository.deleteNotification(notification.getId(),
                            new NotificationRepository.SimpleCallback() {
                                @Override
                                public void onSuccess() {
                                    notifications.remove(notification);
                                    renderNotifications();
                                }

                                @Override
                                public void onError(Exception exception) {
                                    NavigationUtils.showMessage(NotificationsActivity.this, "Không xóa được thông báo");
                                }
                            });
                })
                .show();
    }

    private void openTaskDetail(String taskId) {
        Intent intent = new Intent(this, TaskDetailActivity.class);
        intent.putExtra(TaskDetailActivity.EXTRA_TASK_ID, taskId);
        startActivity(intent);
    }
}
