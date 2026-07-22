package com.example.doancuoiki;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.doancuoiki.model.Task;
import com.example.doancuoiki.repository.TaskRepository;
import com.example.doancuoiki.utils.DateUtils;
import com.example.doancuoiki.utils.VietnameseInputUtils;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.Arrays;
import java.util.Calendar;

public class EditTaskActivity extends Activity {
    public static final String EXTRA_TASK_ID = "editTaskId";

    private final TaskRepository taskRepository = new TaskRepository();

    private EditText titleInput;
    private EditText descriptionInput;
    private EditText dueDateInput;
    private Spinner prioritySpinner;
    private Spinner statusSpinner;
    private Spinner reminderTypeSpinner;
    private EditText edtReminderTime;
    private TextView stateText;
    private TextView saveButton;

    private String taskId;
    private Task currentTask;
    private String currentUserId = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_task);

        View mainLayout = findViewById(R.id.main_layout);
        if (mainLayout != null) {
            ViewCompat.setOnApplyWindowInsetsListener(mainLayout, (v, windowInsets) -> {
                Insets insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars());
                v.setPadding(v.getPaddingLeft(), insets.top, v.getPaddingRight(), insets.bottom);
                return windowInsets;
            });
        }

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            NavigationUtils.showMessage(this, "Cần đăng nhập");
            finish();
            return;
        }
        currentUserId = user.getUid();
        taskId = getIntent().getStringExtra(EXTRA_TASK_ID);

        bindViews();
        setupSpinners();

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
        saveButton.setOnClickListener(v -> saveChanges());
        dueDateInput.setOnClickListener(v -> showDatePicker());
        edtReminderTime.setOnClickListener(v -> showTimePicker());

        loadTask();
    }

    private void bindViews() {
        titleInput = findViewById(R.id.edtTaskTitle);
        descriptionInput = findViewById(R.id.edtTaskDescription);
        dueDateInput = findViewById(R.id.edtTaskDueDate);
        prioritySpinner = findViewById(R.id.spinnerPriority);
        statusSpinner = findViewById(R.id.spinnerStatus);
        reminderTypeSpinner = findViewById(R.id.spinnerReminderType);
        edtReminderTime = findViewById(R.id.edtReminderTime);
        stateText = findViewById(R.id.txtEditState);
        saveButton = findViewById(R.id.btnSaveTask);

        VietnameseInputUtils.setupSingleLine(titleInput);
        VietnameseInputUtils.setupMultiLine(descriptionInput);
    }

    private void setupSpinners() {
        setSpinnerItems(prioritySpinner, Arrays.asList("Thấp", "Trung bình", "Cao"));
        setSpinnerItems(statusSpinner, Arrays.asList(
                Task.STATUS_NOT_STARTED, Task.STATUS_IN_PROGRESS,
                Task.STATUS_DONE, Task.STATUS_PENDING, Task.STATUS_CANCELLED));
        setSpinnerItems(reminderTypeSpinner, Arrays.asList("Không nhắc", "1 giờ", "1 ngày", "3 ngày"));
    }

    private void loadTask() {
        if (taskId == null || taskId.isEmpty()) {
            NavigationUtils.showMessage(this, "Không tìm thấy công việc");
            finish();
            return;
        }
        taskRepository.getTaskById(taskId, new TaskRepository.TaskCallback() {
            @Override
            public void onSuccess(Task task) {
                currentTask = task;
                // Check permission: only creator can edit full task
                if (!currentUserId.equals(task.getCreatorId())) {
                    NavigationUtils.showMessage(EditTaskActivity.this,
                            "Chỉ người tạo công việc được chỉnh sửa");
                    finish();
                    return;
                }
                prefillData(task);
            }

            @Override
            public void onError(Exception exception) {
                NavigationUtils.showMessage(EditTaskActivity.this, "Không tải được công việc");
                finish();
            }
        });
    }

    private void prefillData(Task task) {
        titleInput.setText(task.getTitle() != null ? task.getTitle() : "");
        descriptionInput.setText(task.getDescription() != null ? task.getDescription() : "");
        String due = task.getDueDate();
        if (due != null && !due.equals("Chưa có hạn")) {
            dueDateInput.setText(due);
        }
        setSpinnerValue(prioritySpinner, Arrays.asList("Thấp", "Trung bình", "Cao"), task.getPriority());
        setSpinnerValue(statusSpinner, Arrays.asList(
                Task.STATUS_NOT_STARTED, Task.STATUS_IN_PROGRESS,
                Task.STATUS_DONE, Task.STATUS_PENDING, Task.STATUS_CANCELLED),
                task.getStatus());
        setSpinnerValue(reminderTypeSpinner, Arrays.asList("Không nhắc", "1 giờ", "1 ngày", "3 ngày"),
                task.getReminderType());
        if (task.getReminderTime() != null) {
            edtReminderTime.setText(task.getReminderTime());
        }
    }

    private void saveChanges() {
        if (currentTask == null) return;

        String title = titleInput.getText().toString().trim();
        if (title.isEmpty()) {
            showState("Vui lòng nhập tên công việc");
            return;
        }

        setLoading(true);

        currentTask.setTitle(title);
        currentTask.setDescription(descriptionInput.getText().toString().trim());
        String dueDate = dueDateInput.getText().toString().trim();
        currentTask.setDueDate(dueDate.isEmpty() ? "Chưa có hạn" : dueDate);
        currentTask.setPriority(selectedSpinnerText(prioritySpinner));
        currentTask.setStatus(selectedSpinnerText(statusSpinner));
        currentTask.setReminderType(selectedSpinnerText(reminderTypeSpinner));
        currentTask.setReminderTime(edtReminderTime.getText().toString().trim());

        taskRepository.updateTask(currentTask, new TaskRepository.SimpleCallback() {
            @Override
            public void onSuccess() {
                // Log activity
                com.example.doancuoiki.model.ActivityLog log = new com.example.doancuoiki.model.ActivityLog();
                log.setProjectId(currentTask.getProjectId() != null ? currentTask.getProjectId() : "");
                log.setUserId(currentUserId);
                log.setUserName("Thành viên");
                log.setActionText("đã cập nhật công việc");
                log.setTargetName(currentTask.getTitle());
                log.setType("task");
                log.setTimestamp(new com.google.firebase.Timestamp(new java.util.Date()));
                new com.example.doancuoiki.repository.ProjectRepository().addActivityLog(log, null);

                NavigationUtils.showMessage(EditTaskActivity.this, "Đã lưu thay đổi");
                setResult(RESULT_OK);
                finish();
            }

            @Override
            public void onError(Exception exception) {
                setLoading(false);
                showState("Lưu thất bại: " + exception.getMessage());
            }
        });
    }

    private void showDatePicker() {
        Calendar calendar = Calendar.getInstance();
        new DatePickerDialog(
                this,
                (view, year, month, day) ->
                        dueDateInput.setText(DateUtils.fromCalendarDate(year, month, day)),
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
        ).show();
    }

    private void showTimePicker() {
        Calendar c = Calendar.getInstance();
        new android.app.TimePickerDialog(this, (view, hourOfDay, minute) ->
                edtReminderTime.setText(String.format(java.util.Locale.getDefault(), "%02d:%02d", hourOfDay, minute)),
                c.get(Calendar.HOUR_OF_DAY), c.get(Calendar.MINUTE), true).show();
    }

    private void setSpinnerItems(Spinner spinner, java.util.List<String> items) {
        spinner.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, items));
    }

    private void setSpinnerValue(Spinner spinner, java.util.List<String> items, String value) {
        if (value == null) return;
        int index = items.indexOf(value);
        if (index >= 0) spinner.setSelection(index);
    }

    private String selectedSpinnerText(Spinner spinner) {
        Object item = spinner.getSelectedItem();
        return item == null ? "" : item.toString();
    }

    private void setLoading(boolean loading) {
        saveButton.setEnabled(!loading);
        saveButton.setText(loading ? "Đang lưu..." : "Lưu thay đổi");
    }

    private void showState(String message) {
        stateText.setText(message);
    }
}
