package com.example.doancuoiki;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.PopupMenu;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import android.widget.TextView;

import com.example.doancuoiki.model.Task;
import com.example.doancuoiki.repository.TaskRepository;
import com.example.doancuoiki.NavigationUtils;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class TaskDetailActivity extends Activity {
    public static final String EXTRA_TASK_ID = "taskId";

    private final TaskRepository taskRepository = new TaskRepository();

    private TextView titleText;
    private TextView descriptionText;
    private TextView projectText;
    private TextView assigneeText;
    private TextView dueDateText;
    private TextView priorityText;
    private TextView categoryText;
    private TextView reminderText;
    private TextView createdText;
    private View btnDeleteTask;
    
    private Button saveButton;
    private Task currentTask;
    private String currentUserId = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_task_detail);

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        currentUserId = user == null ? "" : user.getUid();
        View mainLayout = findViewById(R.id.main_layout);
        if (mainLayout != null) {
            ViewCompat.setOnApplyWindowInsetsListener(mainLayout, (v, windowInsets) -> {
                Insets insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars());
                v.setPadding(v.getPaddingLeft(), insets.top, v.getPaddingRight(), insets.bottom);
                return windowInsets;
            });
        }
        bindViews();
        setupActions();
        loadTask();
    }

    private void bindViews() {
        titleText = findViewById(R.id.txtTaskTitle);
        descriptionText = findViewById(R.id.txtTaskDescription);
        projectText = findViewById(R.id.txtTaskProject);
        assigneeText = findViewById(R.id.txtTaskAssignee);
        dueDateText = findViewById(R.id.txtTaskDueDate);
        priorityText = findViewById(R.id.txtTaskPriority);
        categoryText = findViewById(R.id.txtTaskCategory);
        reminderText = findViewById(R.id.txtTaskReminder);
        createdText = findViewById(R.id.txtTaskCreated);
        saveButton = findViewById(R.id.btnSaveTask);
        btnDeleteTask = findViewById(R.id.btnDeleteTask);
    }

    private void setupActions() {
        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
        saveButton.setOnClickListener(v -> showStatusMenu(v));
        
        View statusBadge = findViewById(R.id.statusBadge);
        if (statusBadge != null) {
            statusBadge.setOnClickListener(v -> showStatusMenu(v));
        }

        if (btnDeleteTask != null) {
            btnDeleteTask.setOnClickListener(v -> confirmDeleteTask());
        }
    }

    private void confirmDeleteTask() {
        new android.app.AlertDialog.Builder(this)
            .setTitle("Xóa công việc")
            .setMessage("Bạn có chắc chắn muốn xóa công việc này không?")
            .setPositiveButton("Xóa", (dialog, which) -> {
                if (currentTask != null) {
                    taskRepository.deleteTask(currentTask.getId(), new TaskRepository.SimpleCallback() {
                        @Override
                        public void onSuccess() {
                            com.example.doancuoiki.model.ActivityLog log = new com.example.doancuoiki.model.ActivityLog();
                            log.setProjectId(currentTask.getProjectId() != null ? currentTask.getProjectId() : "");
                            log.setUserName(currentTask.getAssigneeName() != null ? currentTask.getAssigneeName() : "Thành viên");
                            log.setUserId(currentUserId);
                            log.setActionText("đã xóa công việc");
                            log.setTargetName(currentTask.getTitle());
                            log.setType("task");
                            log.setTimestamp(new com.google.firebase.Timestamp(new java.util.Date()));
                            new com.example.doancuoiki.repository.ProjectRepository().addActivityLog(log, null);

                            NavigationUtils.showMessage(TaskDetailActivity.this, "Đã xóa công việc");
                            setResult(RESULT_OK);
                            finish();
                        }
                        @Override
                        public void onError(Exception exception) {
                            NavigationUtils.showMessage(TaskDetailActivity.this, "Lỗi khi xóa: " + exception.getMessage());
                        }
                    });
                }
            })
            .setNegativeButton("Hủy", null)
            .show();
    }
    
    private void showStatusMenu(View v) {
        PopupMenu popup = new PopupMenu(this, v);
        popup.getMenu().add(0, 1, 0, Task.STATUS_NOT_STARTED);
        popup.getMenu().add(0, 2, 1, Task.STATUS_IN_PROGRESS);
        popup.getMenu().add(0, 3, 2, Task.STATUS_DONE);
        popup.getMenu().add(0, 4, 3, Task.STATUS_CANCELLED);
        popup.setOnMenuItemClickListener(item -> {
            currentTask.setStatus(item.getTitle().toString());
            renderTask(); // Update UI
            updateStatusToDB(currentTask.getStatus());
            return true;
        });
        popup.show();
    }
    
    private void updateStatusToDB(String newStatus) {
        if (currentTask == null) return;
        saveButton.setEnabled(false);
        saveButton.setText("Đang lưu...");
        taskRepository.updateTaskStatus(currentTask.getId(), newStatus, new TaskRepository.SimpleCallback() {
            @Override
            public void onSuccess() {
                saveButton.setEnabled(true);
                saveButton.setText("✏ Cập nhật trạng thái");
                NavigationUtils.showMessage(TaskDetailActivity.this, "Cập nhật thành công");
                setResult(RESULT_OK);
            }

            @Override
            public void onError(Exception exception) {
                saveButton.setEnabled(true);
                saveButton.setText("✏ Cập nhật trạng thái");
                NavigationUtils.showMessage(TaskDetailActivity.this, "Lỗi khi cập nhật: " + exception.getMessage());
            }
        });
    }

    private void loadTask() {
        String taskId = getIntent().getStringExtra(EXTRA_TASK_ID);
        if (taskId == null || taskId.trim().isEmpty()) {
            NavigationUtils.showMessage(this, "Không tìm thấy công việc");
            finish();
            return;
        }

        taskRepository.getTaskById(taskId, new TaskRepository.TaskCallback() {
            @Override
            public void onSuccess(Task task) {
                currentTask = task;
                renderTask();
            }

            @Override
            public void onError(Exception exception) {
                NavigationUtils.showMessage(TaskDetailActivity.this,
                        "Không tải được công việc");
                finish();
            }
        });
    }

    private void renderTask() {
        if (currentTask != null && currentUserId != null && currentUserId.equals(currentTask.getCreatorId())) {
            btnDeleteTask.setVisibility(View.VISIBLE);
        } else {
            btnDeleteTask.setVisibility(View.GONE);
        }

        titleText.setText(valueOrDefault(currentTask.getTitle(), "Công việc"));
        descriptionText.setText(valueOrDefault(currentTask.getDescription(), "Chưa có mô tả"));
        projectText.setText(valueOrDefault(currentTask.getProjectName(), "Chưa có dự án"));
        assigneeText.setText(valueOrDefault(currentTask.getAssigneeName(), "Chưa có người"));
        dueDateText.setText(valueOrDefault(currentTask.getDueDate(), "Không có hạn"));
        priorityText.setText(valueOrDefault(currentTask.getPriority(), "Trung bình"));
        categoryText.setText(currentTask.getCategory() != null && !currentTask.getCategory().trim().isEmpty() ? currentTask.getCategory() : "Không");
        
        String reminder = "Không";
        if (currentTask.getReminderTime() != null && !currentTask.getReminderTime().trim().isEmpty()) {
            reminder = currentTask.getReminderTime();
            if (currentTask.getReminderType() != null && !currentTask.getReminderType().equals("Không nhắc")) {
                reminder += " (Nhắc trước " + currentTask.getReminderType() + ")";
            }
        }
        reminderText.setText(reminder);
        
        if (createdText != null) {
            String startDate = valueOrDefault(currentTask.getStartDate(), "");
            createdText.setText("Tạo: " + (startDate.isEmpty() ? "Không xác định" : startDate));
        }
        
        TextView txtTaskStatus = findViewById(R.id.txtTaskStatus);
        if (txtTaskStatus != null) {
            txtTaskStatus.setText(valueOrDefault(currentTask.getStatus(), "Chưa bắt đầu"));
        }
    }

    private String valueOrDefault(String value, String defaultValue) {
        return value == null || value.trim().isEmpty() ? defaultValue : value.trim();
    }
}
