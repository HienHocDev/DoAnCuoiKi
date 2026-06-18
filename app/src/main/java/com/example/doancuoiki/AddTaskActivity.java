package com.example.doancuoiki;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;

import com.example.doancuoiki.model.Project;
import com.example.doancuoiki.model.Task;
import com.example.doancuoiki.model.User;
import com.example.doancuoiki.repository.ProjectRepository;
import com.example.doancuoiki.repository.TaskRepository;
import com.example.doancuoiki.repository.UserRepository;
import com.example.doancuoiki.utils.DateUtils;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class AddTaskActivity extends Activity {
    private static final String GUEST_USER_ID = "guest";

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
    private Spinner statusSpinner;
    private Spinner prioritySpinner;
    private String currentUserId = GUEST_USER_ID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_task);

        currentUserId = currentUserId();
        bindViews();
        setupStaticSpinners();
        setupActions();
        loadProjects();
    }

    private void bindViews() {
        titleInput = findViewById(R.id.edtTaskTitle);
        descriptionInput = findViewById(R.id.edtTaskDescription);
        dueDateInput = findViewById(R.id.edtTaskDueDate);
        projectSpinner = findViewById(R.id.spinnerProject);
        assigneeSpinner = findViewById(R.id.spinnerAssignee);
        statusSpinner = findViewById(R.id.spinnerStatus);
        prioritySpinner = findViewById(R.id.spinnerPriority);
    }

    private void setupActions() {
        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
        findViewById(R.id.btnSaveTask).setOnClickListener(v -> saveTask());
        dueDateInput.setOnClickListener(v -> showDatePicker());
        projectSpinner.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(android.widget.AdapterView<?> parent, android.view.View view, int position, long id) {
                loadAssigneesForSelectedProject();
            }

            @Override
            public void onNothingSelected(android.widget.AdapterView<?> parent) {
            }
        });
    }

    private void setupStaticSpinners() {
        setSpinnerItems(statusSpinner, java.util.Arrays.asList(
                Task.STATUS_NOT_STARTED,
                Task.STATUS_IN_PROGRESS,
                Task.STATUS_DONE
        ));
        setSpinnerItems(prioritySpinner, java.util.Arrays.asList("Thấp", "Trung bình", "Cao"));
    }

    private void loadProjects() {
        setSpinnerItems(projectSpinner, java.util.Collections.singletonList("Đang tải dự án..."));
        projectRepository.getProjectsByUser(currentUserId, new ProjectRepository.ProjectListCallback() {
            @Override
            public void onSuccess(List<Project> loadedProjects) {
                projects.clear();
                projects.addAll(loadedProjects);

                if (projects.isEmpty()) {
                    setSpinnerItems(projectSpinner, java.util.Collections.singletonList("Chưa có dự án"));
                    setFallbackAssignee();
                    return;
                }

                List<String> names = new ArrayList<>();
                for (Project project : projects) {
                    names.add(valueOrDefault(project.getName(), "Dự án chưa đặt tên"));
                }
                setSpinnerItems(projectSpinner, names);
                loadAssigneesForSelectedProject();
            }

            @Override
            public void onError(Exception exception) {
                setSpinnerItems(projectSpinner, java.util.Collections.singletonList("Không tải được dự án"));
                setFallbackAssignee();
            }
        });
    }

    private void loadAssigneesForSelectedProject() {
        Project project = selectedProject();
        if (project == null) {
            setFallbackAssignee();
            return;
        }

        userRepository.getUsersByIds(project.getMembers(), new UserRepository.UserListCallback() {
            @Override
            public void onSuccess(List<User> users) {
                assignees.clear();
                assignees.addAll(users);

                if (assignees.isEmpty()) {
                    setFallbackAssignee();
                    return;
                }

                List<String> names = new ArrayList<>();
                for (User user : assignees) {
                    names.add(valueOrDefault(user.getName(), valueOrDefault(user.getEmail(), user.getId())));
                }
                setSpinnerItems(assigneeSpinner, names);
            }

            @Override
            public void onError(Exception exception) {
                setFallbackAssignee();
            }
        });
    }

    private void setFallbackAssignee() {
        assignees.clear();
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        String name = user != null && user.getEmail() != null ? user.getEmail() : "Người dùng hiện tại";
        assignees.add(new User(currentUserId, name, user != null ? user.getEmail() : "", "Thành viên", "", ""));
        setSpinnerItems(assigneeSpinner, java.util.Collections.singletonList(name));
    }

    private void saveTask() {
        String title = titleInput.getText().toString().trim();
        if (title.isEmpty()) {
            NavigationUtils.showMessage(this, "Vui lòng nhập tên công việc");
            return;
        }

        Project project = selectedProject();
        if (project == null) {
            NavigationUtils.showMessage(this, "Vui lòng tạo hoặc chọn dự án trước");
            return;
        }

        User assignee = selectedAssignee();
        String today = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(new Date());
        Task task = new Task(
                null,
                project.getId(),
                valueOrDefault(project.getName(), "Chưa chọn dự án"),
                title,
                descriptionInput.getText().toString().trim(),
                assignee.getId(),
                valueOrDefault(assignee.getName(), valueOrDefault(assignee.getEmail(), "Chưa phân công")),
                currentUserId,
                selectedSpinnerText(statusSpinner),
                selectedSpinnerText(prioritySpinner),
                today,
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

    private Project selectedProject() {
        int position = projectSpinner.getSelectedItemPosition();
        if (position < 0 || position >= projects.size()) {
            return null;
        }
        return projects.get(position);
    }

    private User selectedAssignee() {
        int position = assigneeSpinner.getSelectedItemPosition();
        if (position < 0 || position >= assignees.size()) {
            return new User(currentUserId, "Người dùng hiện tại", "", "Thành viên", "", "");
        }
        return assignees.get(position);
    }

    private void setSpinnerItems(Spinner spinner, List<String> items) {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_dropdown_item,
                items
        );
        spinner.setAdapter(adapter);
    }

    private String selectedSpinnerText(Spinner spinner) {
        Object item = spinner.getSelectedItem();
        return item == null ? "" : item.toString();
    }

    private String currentUserId() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            return GUEST_USER_ID;
        }
        return user.getUid();
    }

    private String valueOrDefault(String value, String defaultValue) {
        if (value == null || value.trim().isEmpty()) {
            return defaultValue;
        }
        return value.trim();
    }
}
