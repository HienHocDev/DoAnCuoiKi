package com.example.doancuoiki;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import com.example.doancuoiki.model.Task;
import com.example.doancuoiki.repository.TaskRepository;
import com.example.doancuoiki.utils.DateUtils;

import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

public class TaskDetailActivity extends Activity {
    public static final String EXTRA_TASK_ID = "taskId";

    private final TaskRepository taskRepository = new TaskRepository();

    private EditText titleInput;
    private EditText descriptionInput;
    private EditText dueDateInput;
    private TextView projectText;
    private TextView assigneeText;
    private Spinner statusSpinner;
    private Spinner prioritySpinner;
    private Task currentTask;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_task_detail);

        bindViews();
        setupSpinners();
        setupActions();
        loadTask();
    }

    private void bindViews() {
        titleInput = findViewById(R.id.edtTaskTitle);
        descriptionInput = findViewById(R.id.edtTaskDescription);
        dueDateInput = findViewById(R.id.edtTaskDueDate);
        projectText = findViewById(R.id.txtTaskProject);
        assigneeText = findViewById(R.id.txtTaskAssignee);
        statusSpinner = findViewById(R.id.spinnerStatus);
        prioritySpinner = findViewById(R.id.spinnerPriority);
    }

    private void setupActions() {
        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
        findViewById(R.id.btnSaveTask).setOnClickListener(v -> updateTask());
        findViewById(R.id.btnDeleteTask).setOnClickListener(v -> confirmDelete());
        dueDateInput.setOnClickListener(v -> showDatePicker());
    }

    private void setupSpinners() {
        setSpinnerItems(statusSpinner, Arrays.asList(
                Task.STATUS_NOT_STARTED,
                Task.STATUS_IN_PROGRESS,
                Task.STATUS_DONE
        ));
        setSpinnerItems(prioritySpinner, Arrays.asList("Thấp", "Trung bình", "Cao"));
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
                NavigationUtils.showMessage(TaskDetailActivity.this, "Không tải được công việc");
                finish();
            }
        });
    }

    private void renderTask() {
        titleInput.setText(valueOrDefault(currentTask.getTitle(), ""));
        descriptionInput.setText(valueOrDefault(currentTask.getDescription(), ""));
        dueDateInput.setText(valueOrDefault(currentTask.getDueDate(), ""));
        projectText.setText("Dự án: " + valueOrDefault(currentTask.getProjectName(), "Chưa chọn dự án"));
        assigneeText.setText("Người thực hiện: " + valueOrDefault(currentTask.getAssigneeName(), "Chưa phân công"));
        selectSpinnerValue(statusSpinner, currentTask.getStatus());
        selectSpinnerValue(prioritySpinner, currentTask.getPriority());
    }

    private void updateTask() {
        if (currentTask == null) {
            return;
        }

        String title = titleInput.getText().toString().trim();
        if (title.isEmpty()) {
            NavigationUtils.showMessage(this, "Vui lòng nhập tên công việc");
            return;
        }

        currentTask.setTitle(title);
        currentTask.setDescription(descriptionInput.getText().toString().trim());
        currentTask.setDueDate(valueOrDefault(dueDateInput.getText().toString(), "Chưa có hạn"));
        currentTask.setStatus(selectedSpinnerText(statusSpinner));
        currentTask.setPriority(selectedSpinnerText(prioritySpinner));

        taskRepository.updateTask(currentTask, new TaskRepository.SimpleCallback() {
            @Override
            public void onSuccess() {
                NavigationUtils.showMessage(TaskDetailActivity.this, "Đã cập nhật công việc");
                finish();
            }

            @Override
            public void onError(Exception exception) {
                NavigationUtils.showMessage(TaskDetailActivity.this, "Cập nhật thất bại");
            }
        });
    }

    private void confirmDelete() {
        if (currentTask == null) {
            return;
        }

        new AlertDialog.Builder(this)
                .setTitle("Xóa công việc")
                .setMessage("Bạn có chắc muốn xóa công việc này không?")
                .setNegativeButton("Hủy", null)
                .setPositiveButton("Xóa", (dialog, which) -> deleteTask())
                .show();
    }

    private void deleteTask() {
        taskRepository.deleteTask(currentTask.getId(), new TaskRepository.SimpleCallback() {
            @Override
            public void onSuccess() {
                NavigationUtils.showMessage(TaskDetailActivity.this, "Đã xóa công việc");
                finish();
            }

            @Override
            public void onError(Exception exception) {
                NavigationUtils.showMessage(TaskDetailActivity.this, "Xóa công việc thất bại");
            }
        });
    }

    private void showDatePicker() {
        Calendar calendar = Calendar.getInstance();
        new DatePickerDialog(
                this,
                (view, year, month, dayOfMonth) ->
                        dueDateInput.setText(DateUtils.fromCalendarDate(year, month, dayOfMonth)),
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
        ).show();
    }

    private void setSpinnerItems(Spinner spinner, List<String> items) {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_dropdown_item,
                items
        );
        spinner.setAdapter(adapter);
    }

    private void selectSpinnerValue(Spinner spinner, String value) {
        if (value == null) {
            return;
        }
        for (int i = 0; i < spinner.getCount(); i++) {
            if (value.equals(spinner.getItemAtPosition(i).toString())) {
                spinner.setSelection(i);
                return;
            }
        }
    }

    private String selectedSpinnerText(Spinner spinner) {
        Object item = spinner.getSelectedItem();
        return item == null ? "" : item.toString();
    }

    private String valueOrDefault(String value, String defaultValue) {
        if (value == null || value.trim().isEmpty()) {
            return defaultValue;
        }
        return value.trim();
    }
}
