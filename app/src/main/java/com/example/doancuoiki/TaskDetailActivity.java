package com.example.doancuoiki;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;

import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.doancuoiki.model.Task;
import com.example.doancuoiki.repository.TaskRepository;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class TaskDetailActivity extends Activity {
    public static final String EXTRA_TASK_ID = "taskId";
    private static final int REQUEST_EDIT_TASK = 301;

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
    private View btnEditTask;
    private Button saveButton;

    // B8 — Comment section
    private LinearLayout commentList;
    private EditText commentInput;
    private Button btnSendComment;

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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_EDIT_TASK && resultCode == RESULT_OK) {
            loadTask(); // Reload task after edit
        }
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
        btnEditTask = findViewById(R.id.btnEditTask);

        // B8 — Comments
        commentList = findViewById(R.id.commentList);
        commentInput = findViewById(R.id.edtComment);
        btnSendComment = findViewById(R.id.btnSendComment);
    }

    private void setupActions() {
        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
        if (saveButton != null) saveButton.setOnClickListener(v -> showStatusMenu(v));

        View statusBadge = findViewById(R.id.statusBadge);
        if (statusBadge != null) {
            statusBadge.setOnClickListener(v -> showStatusMenu(v));
        }

        if (btnDeleteTask != null) {
            btnDeleteTask.setOnClickListener(v -> confirmDeleteTask());
        }

        if (btnEditTask != null) {
            btnEditTask.setOnClickListener(v -> openEditTask());
        }

        // B8 — Comment send
        if (btnSendComment != null) {
            btnSendComment.setOnClickListener(v -> sendComment());
        }
    }

    private void openEditTask() {
        if (currentTask == null) return;
        if (!currentUserId.equals(currentTask.getCreatorId())) {
            NavigationUtils.showMessage(this, "Chỉ người tạo công việc được chỉnh sửa");
            return;
        }
        Intent intent = new Intent(this, EditTaskActivity.class);
        intent.putExtra(EditTaskActivity.EXTRA_TASK_ID, currentTask.getId());
        startActivityForResult(intent, REQUEST_EDIT_TASK);
    }

    private void confirmDeleteTask() {
        new AlertDialog.Builder(this)
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
        // Both creator and assignee can update status
        if (currentTask == null) return;
        boolean canUpdate = currentUserId.equals(currentTask.getAssigneeId())
                || currentUserId.equals(currentTask.getCreatorId());
        if (!canUpdate) {
            NavigationUtils.showMessage(this, "Bạn không có quyền thay đổi trạng thái");
            return;
        }

        PopupMenu popup = new PopupMenu(this, v);
        popup.getMenu().add(0, 1, 0, Task.STATUS_NOT_STARTED);
        popup.getMenu().add(0, 2, 1, Task.STATUS_IN_PROGRESS);
        popup.getMenu().add(0, 3, 2, Task.STATUS_DONE);
        popup.getMenu().add(0, 4, 3, Task.STATUS_CANCELLED);
        popup.setOnMenuItemClickListener(item -> {
            currentTask.setStatus(item.getTitle().toString());
            renderTask();
            updateStatusToDB(currentTask.getStatus());
            return true;
        });
        popup.show();
    }

    private void updateStatusToDB(String newStatus) {
        if (currentTask == null) return;
        if (saveButton != null) {
            saveButton.setEnabled(false);
            saveButton.setText("Đang lưu...");
        }
        taskRepository.updateTaskStatus(currentTask.getId(), newStatus, new TaskRepository.SimpleCallback() {
            @Override
            public void onSuccess() {
                if (saveButton != null) {
                    saveButton.setEnabled(true);
                    saveButton.setText("✏ Cập nhật trạng thái");
                }
                // Log activity
                com.example.doancuoiki.model.ActivityLog log = new com.example.doancuoiki.model.ActivityLog();
                log.setProjectId(currentTask.getProjectId() != null ? currentTask.getProjectId() : "");
                log.setUserId(currentUserId);
                log.setUserName("Thành viên");
                log.setActionText("đã cập nhật trạng thái → " + newStatus);
                log.setTargetName(currentTask.getTitle());
                log.setType("task");
                log.setTimestamp(new com.google.firebase.Timestamp(new java.util.Date()));
                new com.example.doancuoiki.repository.ProjectRepository().addActivityLog(log, null);

                NavigationUtils.showMessage(TaskDetailActivity.this, "Cập nhật thành công");
                setResult(RESULT_OK);
            }

            @Override
            public void onError(Exception exception) {
                if (saveButton != null) {
                    saveButton.setEnabled(true);
                    saveButton.setText("✏ Cập nhật trạng thái");
                }
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
                renderComments(task.getComments());
            }

            @Override
            public void onError(Exception exception) {
                NavigationUtils.showMessage(TaskDetailActivity.this, "Không tải được công việc");
                finish();
            }
        });
    }

    private void renderTask() {
        boolean isCreator = currentTask != null && currentUserId.equals(currentTask.getCreatorId());
        boolean isAssignee = currentTask != null && currentUserId.equals(currentTask.getAssigneeId());

        if (btnDeleteTask != null) btnDeleteTask.setVisibility(isCreator ? View.VISIBLE : View.GONE);
        if (btnEditTask != null) btnEditTask.setVisibility(isCreator ? View.VISIBLE : View.GONE);

        titleText.setText(valueOrDefault(currentTask.getTitle(), "Công việc"));
        descriptionText.setText(valueOrDefault(currentTask.getDescription(), "Chưa có mô tả"));
        projectText.setText(valueOrDefault(currentTask.getProjectName(), "Chưa có dự án"));
        assigneeText.setText(valueOrDefault(currentTask.getAssigneeName(), "Chưa có người"));
        dueDateText.setText(valueOrDefault(currentTask.getDueDate(), "Không có hạn"));
        priorityText.setText(valueOrDefault(currentTask.getPriority(), "Trung bình"));
        categoryText.setText(currentTask.getCategory() != null && !currentTask.getCategory().trim().isEmpty()
                ? currentTask.getCategory() : "Không");

        String reminder = "Không";
        if (currentTask.getReminderTime() != null && !currentTask.getReminderTime().trim().isEmpty()) {
            reminder = currentTask.getReminderTime();
            if (currentTask.getReminderType() != null && !currentTask.getReminderType().equals("Không nhắc")) {
                reminder += " (Nhắc trước " + currentTask.getReminderType() + ")";
            }
        }
        if (reminderText != null) reminderText.setText(reminder);

        if (createdText != null) {
            String startDate = valueOrDefault(currentTask.getStartDate(), "");
            createdText.setText("Tạo: " + (startDate.isEmpty() ? "Không xác định" : startDate));
        }

        TextView txtTaskStatus = findViewById(R.id.txtTaskStatus);
        if (txtTaskStatus != null) {
            txtTaskStatus.setText(valueOrDefault(currentTask.getStatus(), "Chưa bắt đầu"));
        }
    }

    // ─── B8 — Comment Section ───────────────────────────────────────────────

    private void renderComments(List<String> comments) {
        if (commentList == null) return;
        commentList.removeAllViews();

        if (comments == null || comments.isEmpty()) {
            TextView empty = new TextView(this);
            empty.setText("Chưa có bình luận nào.");
            empty.setTextColor(Color.parseColor("#7D8496"));
            empty.setTextSize(13);
            empty.setPadding(0, 8, 0, 8);
            commentList.addView(empty);
            return;
        }

        for (String comment : comments) {
            View card = buildCommentCard(comment);
            commentList.addView(card);
        }
    }

    private View buildCommentCard(String comment) {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.VERTICAL);
        row.setPadding(0, 8, 0, 8);

        TextView tv = new TextView(this);
        tv.setText(comment);
        tv.setTextColor(Color.parseColor("#222632"));
        tv.setTextSize(14);

        // Divider
        View divider = new View(this);
        divider.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, 1));
        divider.setBackgroundColor(Color.parseColor("#E5E7EB"));

        row.addView(tv);
        row.addView(divider);
        return row;
    }

    private void sendComment() {
        if (currentTask == null) return;
        if (commentInput == null) return;

        String text = commentInput.getText().toString().trim();
        if (text.isEmpty()) {
            NavigationUtils.showMessage(this, "Nhập bình luận trước khi gửi");
            return;
        }

        // Format: "HH:mm Tên: nội dung"
        String timestamp = new SimpleDateFormat("HH:mm dd/MM", Locale.getDefault()).format(new Date());
        // Try to get user name from task
        String userName = "Thành viên";
        if (currentTask != null && currentUserId.equals(currentTask.getAssigneeId())) {
            userName = valueOrDefault(currentTask.getAssigneeName(), "Thành viên");
        }
        String formatted = "[" + timestamp + "] " + userName + ": " + text;

        btnSendComment.setEnabled(false);
        taskRepository.addComment(currentTask.getId(), formatted, new TaskRepository.SimpleCallback() {
            @Override
            public void onSuccess() {
                commentInput.setText("");
                btnSendComment.setEnabled(true);
                // Add to local list for immediate display
                List<String> currentComments = currentTask.getComments() != null
                        ? new ArrayList<>(currentTask.getComments()) : new ArrayList<>();
                currentComments.add(formatted);
                currentTask.setComments(currentComments);
                renderComments(currentComments);
            }

            @Override
            public void onError(Exception exception) {
                btnSendComment.setEnabled(true);
                NavigationUtils.showMessage(TaskDetailActivity.this, "Không gửi được bình luận");
            }
        });
    }

    private String valueOrDefault(String value, String defaultValue) {
        return value == null || value.trim().isEmpty() ? defaultValue : value.trim();
    }
}
