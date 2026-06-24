package com.example.doancuoiki;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;

import com.example.doancuoiki.model.Task;
import com.example.doancuoiki.repository.TaskRepository;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.Arrays;

public class TaskDetailActivity extends Activity {
    public static final String EXTRA_TASK_ID = "taskId";

    private final TaskRepository taskRepository = new TaskRepository();

    private TextView titleText;
    private TextView descriptionText;
    private TextView projectText;
    private TextView assigneeText;
    private TextView dueDateText;
    private TextView priorityText;
    private TextView permissionText;
    private Spinner statusSpinner;
    private Button saveButton;
    private Task currentTask;
    private String currentUserId = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_task_detail);

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        currentUserId = user == null ? "" : user.getUid();
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
        permissionText = findViewById(R.id.txtTaskPermission);
        statusSpinner = findViewById(R.id.spinnerStatus);
        saveButton = findViewById(R.id.btnSaveTask);

        statusSpinner.setAdapter(new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_dropdown_item,
                Arrays.asList(
                        Task.STATUS_NOT_STARTED,
                        Task.STATUS_IN_PROGRESS,
                        Task.STATUS_DONE
                )
        ));
    }

    private void setupActions() {
        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
        saveButton.setOnClickListener(v -> updateStatus());
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
        titleText.setText(valueOrDefault(currentTask.getTitle(), "Công việc"));
        descriptionText.setText(valueOrDefault(currentTask.getDescription(), "Chưa có mô tả"));
        projectText.setText("Dự án: "
                + valueOrDefault(currentTask.getProjectName(), "Chưa xác định"));
        assigneeText.setText("Người thực hiện: "
                + valueOrDefault(currentTask.getAssigneeName(), "Chưa phân công"));
        dueDateText.setText("Hạn hoàn thành: "
                + valueOrDefault(currentTask.getDueDate(), "Chưa có hạn"));
        priorityText.setText("Độ ưu tiên: "
                + valueOrDefault(currentTask.getPriority(), "Trung bình"));
        selectSpinnerValue(currentTask.getStatus());

        boolean isAssignee = currentUserId.equals(currentTask.getAssigneeId());
        statusSpinner.setEnabled(isAssignee);
        saveButton.setVisibility(isAssignee ? View.VISIBLE : View.GONE);
        permissionText.setText(isAssignee
                ? "Bạn có thể cập nhật trạng thái công việc này."
                : "Chỉ thành viên được giao việc mới có thể cập nhật trạng thái.");
    }

    private void updateStatus() {
        if (currentTask == null || !currentUserId.equals(currentTask.getAssigneeId())) {
            NavigationUtils.showMessage(this,
                    "Bạn không có quyền cập nhật công việc này");
            return;
        }

        String status = statusSpinner.getSelectedItem().toString();
        taskRepository.updateTaskStatus(currentTask.getId(), status,
                new TaskRepository.SimpleCallback() {
                    @Override
                    public void onSuccess() {
                        NavigationUtils.showMessage(TaskDetailActivity.this,
                                "Đã cập nhật trạng thái");
                        finish();
                    }

                    @Override
                    public void onError(Exception exception) {
                        NavigationUtils.showMessage(TaskDetailActivity.this,
                                "Cập nhật trạng thái thất bại");
                    }
                });
    }

    private void selectSpinnerValue(String value) {
        if (value == null) {
            return;
        }
        for (int i = 0; i < statusSpinner.getCount(); i++) {
            if (value.equals(statusSpinner.getItemAtPosition(i).toString())) {
                statusSpinner.setSelection(i);
                return;
            }
        }
    }

    private String valueOrDefault(String value, String defaultValue) {
        return value == null || value.trim().isEmpty() ? defaultValue : value.trim();
    }
}
