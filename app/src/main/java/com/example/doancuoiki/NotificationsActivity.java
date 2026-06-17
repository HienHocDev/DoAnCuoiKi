package com.example.doancuoiki;

import android.app.Activity;
import android.os.Bundle;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.doancuoiki.model.NotificationItem;
import com.example.doancuoiki.model.Task;
import com.example.doancuoiki.repository.NotificationRepository;
import com.example.doancuoiki.repository.TaskRepository;
import com.example.doancuoiki.utils.DateUtils;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;
import java.util.List;

public class NotificationsActivity extends Activity {
    private static final String GUEST_USER_ID = "guest";

    private final NotificationRepository notificationRepository = new NotificationRepository();
    private final TaskRepository taskRepository = new TaskRepository();
    private final List<NotificationItem> notifications = new ArrayList<>();

    private LinearLayout notificationList;
    private TextView notificationState;
    private String currentUserId = GUEST_USER_ID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notifications);

        notificationList = findViewById(R.id.notificationList);
        notificationState = findViewById(R.id.txtNotificationState);
        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        resolveCurrentUser();
        loadNotifications();
    }

    private void resolveCurrentUser() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            currentUserId = user.getUid();
        }
    }

    private void loadNotifications() {
        notificationState.setText("Đang tải thông báo...");
        notifications.clear();

        notificationRepository.getNotificationsByUser(currentUserId, new NotificationRepository.NotificationListCallback() {
            @Override
            public void onSuccess(List<NotificationItem> firestoreNotifications) {
                notifications.addAll(firestoreNotifications);
                loadTaskBasedNotifications();
            }

            @Override
            public void onError(Exception exception) {
                loadTaskBasedNotifications();
            }
        });
    }

    private void loadTaskBasedNotifications() {
        taskRepository.getAllTasks(new TaskRepository.TaskListCallback() {
            @Override
            public void onSuccess(List<Task> tasks) {
                notifications.addAll(createNotificationsFromTasks(tasks));
                renderNotifications();
            }

            @Override
            public void onError(Exception exception) {
                renderNotifications();
            }
        });
    }

    private List<NotificationItem> createNotificationsFromTasks(List<Task> tasks) {
        List<NotificationItem> generated = new ArrayList<>();
        for (Task task : tasks) {
            if (currentUserId.equals(task.getAssigneeId())) {
                generated.add(new NotificationItem(
                        "task-" + task.getId(),
                        currentUserId,
                        "Bạn được giao công việc",
                        task.getTitle() + " - " + task.getProjectName(),
                        "task_assigned",
                        false,
                        "Tự động"
                ));
            }

            if (!Task.STATUS_DONE.equals(task.getStatus()) && DateUtils.isDueSoon(task.getDueDate(), 3)) {
                generated.add(new NotificationItem(
                        "deadline-" + task.getId(),
                        currentUserId,
                        "Công việc sắp đến hạn",
                        task.getTitle() + " đến hạn vào " + task.getDueDate(),
                        "task_due_soon",
                        false,
                        "Tự động"
                ));
            }

            if (Task.STATUS_DONE.equals(task.getStatus())) {
                generated.add(new NotificationItem(
                        "done-" + task.getId(),
                        currentUserId,
                        "Công việc đã hoàn thành",
                        task.getTitle() + " trong dự án " + task.getProjectName(),
                        "task_done",
                        true,
                        "Tự động"
                ));
            }
        }
        return generated;
    }

    private void renderNotifications() {
        notificationList.removeAllViews();

        if (notifications.isEmpty()) {
            notificationState.setText("Chưa có thông báo.");
            return;
        }

        notificationState.setText("Có " + notifications.size() + " thông báo.");
        for (NotificationItem notification : notifications) {
            notificationList.addView(ViewFactory.notificationCard(
                    this,
                    notification.getTitle(),
                    notification.getMessage(),
                    notification.isRead() ? "Đã đọc" : notification.getCreatedAt()
            ));
        }
    }

}
