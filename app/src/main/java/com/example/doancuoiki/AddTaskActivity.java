package com.example.doancuoiki;

import android.app.Activity;
import android.os.Bundle;
import android.widget.EditText;

import com.example.doancuoiki.model.Task;
import com.example.doancuoiki.repository.TaskRepository;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class AddTaskActivity extends Activity {
    private final TaskRepository taskRepository = new TaskRepository();

    private EditText titleInput;
    private EditText descriptionInput;
    private EditText projectInput;
    private EditText assigneeInput;
    private EditText dueDateInput;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_task);

        bindViews();
        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
        findViewById(R.id.btnSaveTask).setOnClickListener(v -> saveTask());
    }

    private void bindViews() {
        titleInput = findViewById(R.id.edtTaskTitle);
        descriptionInput = findViewById(R.id.edtTaskDescription);
        projectInput = findViewById(R.id.edtTaskProject);
        assigneeInput = findViewById(R.id.edtTaskAssignee);
        dueDateInput = findViewById(R.id.edtTaskDueDate);
    }

    private void saveTask() {
        String title = titleInput.getText().toString().trim();
        if (title.isEmpty()) {
            NavigationUtils.showMessage(this, "Vui lòng nhập tên công việc");
            return;
        }

        String currentUserId = currentUserId();
        Task task = new Task(
                null,
                "",
                valueOrDefault(projectInput.getText().toString(), "Chưa chọn dự án"),
                title,
                descriptionInput.getText().toString().trim(),
                currentUserId,
                valueOrDefault(assigneeInput.getText().toString(), "Nguyễn Văn A"),
                currentUserId,
                Task.STATUS_NOT_STARTED,
                "Trung bình",
                "",
                valueOrDefault(dueDateInput.getText().toString(), "Chưa có hạn")
        );

        taskRepository.addTask(task, new TaskRepository.SimpleCallback() {
            @Override
            public void onSuccess() {
                NavigationUtils.showMessage(AddTaskActivity.this, "Đã lưu công việc");
                finish();
            }

            @Override
            public void onError(Exception exception) {
                NavigationUtils.showMessage(AddTaskActivity.this, "Chưa lưu được Firestore");
            }
        });
    }

    private String currentUserId() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            return "u001";
        }
        return user.getUid();
    }

    private String valueOrDefault(String value, String defaultValue) {
        String trimmed = value.trim();
        if (trimmed.isEmpty()) {
            return defaultValue;
        }
        return trimmed;
    }
}
