package com.example.doancuoiki;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import com.example.doancuoiki.model.Project;
import com.example.doancuoiki.model.Task;
import com.example.doancuoiki.model.User;
import com.example.doancuoiki.repository.ProjectRepository;
import com.example.doancuoiki.repository.TaskRepository;
import com.example.doancuoiki.repository.UserRepository;
import com.example.doancuoiki.utils.DateUtils;
import com.example.doancuoiki.utils.VietnameseInputUtils;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class AddTaskActivity extends Activity {
    public static final String EXTRA_PROJECT_ID = "addTaskProjectId";

    private final TaskRepository taskRepository = new TaskRepository();
    private final ProjectRepository projectRepository = new ProjectRepository();
    private final UserRepository userRepository = new UserRepository();
    private final List<Project> projects = new ArrayList<>();
    private final List<User> assignees = new ArrayList<>();

    private EditText titleInput;
    private EditText descriptionInput;
    private EditText dueDateInput;
    private Spinner projectSpinner;
    private Spinner assigneeSpinner;
    private Spinner prioritySpinner;
    private Spinner categorySpinner;
    private Spinner reminderTypeSpinner;
    private EditText edtReminderTime;
    private TextView txtCharCount;
    private String currentUserId = "";
    private String requestedProjectId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_task);

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            NavigationUtils.showMessage(this, "Bạn cần đăng nhập để giao công việc");
            finish();
            return;
        }
        currentUserId = user.getUid();
        requestedProjectId = getIntent().getStringExtra(EXTRA_PROJECT_ID);

        bindViews();
        setupSpinners();
        setupActions();
        if (hasRequestedProject()) {
            loadRequestedProject();
        } else {
            loadOwnedProjects();
        }
    }

    private void bindViews() {
        titleInput = findViewById(R.id.edtTaskTitle);
        descriptionInput = findViewById(R.id.edtTaskDescription);
        dueDateInput = findViewById(R.id.edtTaskDueDate);
        projectSpinner = findViewById(R.id.spinnerProject);
        assigneeSpinner = findViewById(R.id.spinnerAssignee);
        prioritySpinner = findViewById(R.id.spinnerPriority);
        categorySpinner = findViewById(R.id.spinnerCategory);
        reminderTypeSpinner = findViewById(R.id.spinnerReminderType);
        edtReminderTime = findViewById(R.id.edtReminderTime);
        txtCharCount = findViewById(R.id.txtCharCount);

        VietnameseInputUtils.setupSingleLine(titleInput);
        VietnameseInputUtils.setupMultiLine(descriptionInput);
    }

    private void setupActions() {
        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
        findViewById(R.id.btnSaveTask).setOnClickListener(v -> saveTask());
        dueDateInput.setOnClickListener(v -> showDatePicker());
        
        edtReminderTime.setOnClickListener(v -> {
            java.util.Calendar c = java.util.Calendar.getInstance();
            new android.app.TimePickerDialog(this, (view, hourOfDay, minute) -> {
                edtReminderTime.setText(String.format(java.util.Locale.getDefault(), "%02d:%02d", hourOfDay, minute));
            }, c.get(java.util.Calendar.HOUR_OF_DAY), c.get(java.util.Calendar.MINUTE), true).show();
        });
        projectSpinner.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(android.widget.AdapterView<?> parent, View view,
                                       int position, long id) {
                loadAssigneesForSelectedProject();
            }

            @Override
            public void onNothingSelected(android.widget.AdapterView<?> parent) {
            }
        });
    }

    private void setupSpinners() {
        setSpinnerItems(prioritySpinner, Arrays.asList("Thấp", "Trung bình", "Cao"));
        setSpinnerItems(categorySpinner, Arrays.asList("Học tập", "Công việc", "Cá nhân"));
        setSpinnerItems(reminderTypeSpinner, Arrays.asList("Không nhắc", "1 giờ", "1 ngày", "3 ngày"));
    }

    private void loadOwnedProjects() {
        projectSpinner.setEnabled(true);
        projectSpinner.setAlpha(1f);
        setSpinnerItems(projectSpinner, Collections.singletonList("Đang tải dự án..."));
        projectRepository.getProjectsOwnedBy(currentUserId,
                new ProjectRepository.ProjectListCallback() {
                    @Override
                    public void onSuccess(List<Project> loadedProjects) {
                        projects.clear();
                        projects.addAll(loadedProjects);
                        if (projects.isEmpty()) {
                            setSpinnerItems(projectSpinner,
                                    Collections.singletonList("Bạn chưa có dự án do mình làm chủ"));
                            setSpinnerItems(assigneeSpinner,
                                    Collections.singletonList("Chưa có thành viên"));
                            return;
                        }

                        List<String> names = new ArrayList<>();
                        int selectedIndex = 0;
                        for (int i = 0; i < projects.size(); i++) {
                            Project project = projects.get(i);
                            names.add(valueOrDefault(project.getName(), "Dự án chưa đặt tên"));
                            if (project.getId().equals(requestedProjectId)) {
                                selectedIndex = i;
                            }
                        }
                        setSpinnerItems(projectSpinner, names);
                        projectSpinner.setSelection(selectedIndex);
                    }

                    @Override
                    public void onError(Exception exception) {
                        setSpinnerItems(projectSpinner,
                                Collections.singletonList("Không tải được dự án"));
                    }
                });
    }

    private void loadRequestedProject() {
        projectSpinner.setEnabled(false);
        projectSpinner.setAlpha(0.75f);
        setSpinnerItems(projectSpinner, Collections.singletonList("Đang tải dự án..."));

        projectRepository.getProjectById(requestedProjectId,
                new ProjectRepository.ProjectCallback() {
                    @Override
                    public void onSuccess(Project project) {
                        if (!currentUserId.equals(project.getOwnerId())) {
                            setSpinnerItems(projectSpinner,
                                    Collections.singletonList("Bạn không có quyền giao việc"));
                            NavigationUtils.showMessage(AddTaskActivity.this,
                                    "Chỉ chủ dự án được thêm công việc");
                            return;
                        }

                        projects.clear();
                        projects.add(project);
                        setSpinnerItems(projectSpinner, Collections.singletonList(
                                valueOrDefault(project.getName(), "Dự án")));
                        projectSpinner.setSelection(0);
                        loadAssigneesForSelectedProject();
                    }

                    @Override
                    public void onError(Exception exception) {
                        projects.clear();
                        setSpinnerItems(projectSpinner,
                                Collections.singletonList("Không tải được dự án"));
                        NavigationUtils.showMessage(AddTaskActivity.this,
                                "Không tìm thấy dự án cần giao việc");
                    }
                });
    }

    private void loadAssigneesForSelectedProject() {
        Project project = selectedProject();
        if (project == null || !currentUserId.equals(project.getOwnerId())) {
            assignees.clear();
            setSpinnerItems(assigneeSpinner, Collections.singletonList("Chưa có thành viên"));
            return;
        }

        userRepository.getUsersByIds(project.getMembers(),
                new UserRepository.UserListCallback() {
                    @Override
                    public void onSuccess(List<User> users) {
                        assignees.clear();
                        assignees.addAll(users);
                        List<String> names = new ArrayList<>();
                        for (User user : users) {
                            names.add(valueOrDefault(user.getName(),
                                    valueOrDefault(user.getEmail(), user.getId())));
                        }
                        setSpinnerItems(assigneeSpinner, names.isEmpty()
                                ? Collections.singletonList("Chưa có thành viên") : names);
                    }

                    @Override
                    public void onError(Exception exception) {
                        assignees.clear();
                        setSpinnerItems(assigneeSpinner,
                                Collections.singletonList("Không tải được thành viên"));
                    }
                });
    }

    private void saveTask() {
        String title = titleInput.getText().toString().trim();
        Project project = selectedProject();
        User assignee = selectedAssignee();

        if (title.isEmpty()) {
            NavigationUtils.showMessage(this, "Vui lòng nhập tên công việc");
            return;
        }
        if (project == null || !currentUserId.equals(project.getOwnerId())) {
            NavigationUtils.showMessage(this, "Bạn không có quyền giao việc trong dự án này");
            return;
        }
        if (assignee == null) {
            NavigationUtils.showMessage(this, "Vui lòng chọn thành viên thực hiện");
            return;
        }

        String today = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(new Date());
        Task task = new Task(
                null,
                project.getId(),
                valueOrDefault(project.getName(), "Dự án"),
                title,
                descriptionInput.getText().toString().trim(),
                assignee.getId(),
                valueOrDefault(assignee.getName(),
                        valueOrDefault(assignee.getEmail(), "Thành viên")),
                currentUserId,
                Task.STATUS_NOT_STARTED,
                selectedSpinnerText(prioritySpinner),
                today,
                valueOrDefault(dueDateInput.getText().toString(), "Chưa có hạn")
        );

        taskRepository.addTask(task, new TaskRepository.SimpleCallback() {
            @Override
            public void onSuccess() {
                NavigationUtils.showMessage(AddTaskActivity.this, "Đã giao công việc");
                finish();
            }

            @Override
            public void onError(Exception exception) {
                NavigationUtils.showMessage(AddTaskActivity.this,
                        "Không lưu được công việc");
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

    private Project selectedProject() {
        if (hasRequestedProject()) {
            return projects.isEmpty() ? null : projects.get(0);
        }
        int position = projectSpinner.getSelectedItemPosition();
        return position < 0 || position >= projects.size() ? null : projects.get(position);
    }

    private boolean hasRequestedProject() {
        return requestedProjectId != null && !requestedProjectId.trim().isEmpty();
    }

    private User selectedAssignee() {
        int position = assigneeSpinner.getSelectedItemPosition();
        return position < 0 || position >= assignees.size() ? null : assignees.get(position);
    }

    private void setSpinnerItems(Spinner spinner, List<String> items) {
        spinner.setAdapter(new ArrayAdapter<>(
                this, android.R.layout.simple_spinner_dropdown_item, items));
    }

    private String selectedSpinnerText(Spinner spinner) {
        Object item = spinner.getSelectedItem();
        return item == null ? "" : item.toString();
    }

    private String valueOrDefault(String value, String defaultValue) {
        return value == null || value.trim().isEmpty() ? defaultValue : value.trim();
    }
}
