package com.example.doancuoiki;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import com.example.doancuoiki.model.Task;
import com.example.doancuoiki.repository.TaskRepository;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class TasksActivity extends Activity {
    private static final String FILTER_ALL = "all";
    private static final String FILTER_MINE = "mine";
    private static final String FILTER_ASSIGNED = "assigned";
    private static final String FILTER_PROJECT = "project";
    private static final String GUEST_USER_ID = "guest";

    private final TaskRepository taskRepository = new TaskRepository();
    private final List<Task> allTasks = new ArrayList<>();

    private LinearLayout taskList;
    private TextView taskState;
    private TextView tabAll;
    private TextView tabMine;
    private TextView tabAssigned;
    private TextView tabProject;
    private EditText searchInput;
    private Spinner projectFilterSpinner;

    private String currentFilter = FILTER_ALL;
    private String currentKeyword = "";
    private String currentUserId = GUEST_USER_ID;
    private String selectedProjectId = "";
    private final List<String> projectFilterIds = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tasks);
        NavigationUtils.setupBottomNav(this, NavigationUtils.TASKS);

        bindViews();
        setupActions();
        resolveCurrentUser();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (taskList != null) {
            loadTasks();
        }
    }

    private void bindViews() {
        taskList = findViewById(R.id.taskList);
        taskState = findViewById(R.id.txtTaskState);
        searchInput = findViewById(R.id.edtSearchTask);
        tabAll = findViewById(R.id.tabAll);
        tabMine = findViewById(R.id.tabMine);
        tabAssigned = findViewById(R.id.tabAssigned);
        tabProject = findViewById(R.id.tabProject);
        projectFilterSpinner = findViewById(R.id.spinnerProjectFilter);
    }

    private void setupActions() {
        findViewById(R.id.btnAddTask).setOnClickListener(v ->
                NavigationUtils.open(this, AddTaskActivity.class));

        tabAll.setOnClickListener(v -> changeFilter(FILTER_ALL));
        tabMine.setOnClickListener(v -> changeFilter(FILTER_MINE));
        tabAssigned.setOnClickListener(v -> changeFilter(FILTER_ASSIGNED));
        tabProject.setOnClickListener(v -> changeFilter(FILTER_PROJECT));
        projectFilterSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position >= 0 && position < projectFilterIds.size()) {
                    selectedProjectId = projectFilterIds.get(position);
                    renderTasks();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        searchInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                currentKeyword = s.toString().trim().toLowerCase();
                renderTasks();
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
    }

    private void resolveCurrentUser() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            currentUserId = user.getUid();
        }
    }

    private void loadTasks() {
        taskState.setText("Đang tải công việc...");
        taskRepository.getTasksForUser(currentUserId, new TaskRepository.TaskListCallback() {
            @Override
            public void onSuccess(List<Task> tasks) {
                allTasks.clear();
                if (tasks.isEmpty()) {
                    taskState.setText("Chưa có công việc nào. Hãy thêm công việc đầu tiên.");
                } else {
                    allTasks.addAll(tasks);
                    taskState.setText("Đã tải " + tasks.size() + " công việc từ Firestore.");
                }
                setupProjectFilter();
                renderTasks();
            }

            @Override
            public void onError(Exception exception) {
                allTasks.clear();
                setupProjectFilter();
                taskState.setText("Không tải được công việc từ Firestore.");
                renderTasks();
            }
        });
    }

    private void changeFilter(String filter) {
        currentFilter = filter;
        highlightSelectedTab();
        renderTasks();
    }

    private void highlightSelectedTab() {
        setTabState(tabAll, FILTER_ALL.equals(currentFilter));
        setTabState(tabMine, FILTER_MINE.equals(currentFilter));
        setTabState(tabAssigned, FILTER_ASSIGNED.equals(currentFilter));
        setTabState(tabProject, FILTER_PROJECT.equals(currentFilter));
        projectFilterSpinner.setVisibility(FILTER_PROJECT.equals(currentFilter) ? View.VISIBLE : View.GONE);
    }

    private void setTabState(TextView tab, boolean selected) {
        tab.setTextColor(selected ? Color.rgb(34, 197, 94) : Color.rgb(125, 132, 150));
        tab.setTypeface(null, selected ? android.graphics.Typeface.BOLD : android.graphics.Typeface.NORMAL);
    }

    private void renderTasks() {
        taskList.removeAllViews();
        highlightSelectedTab();

        List<Task> filteredTasks = filterTasks();
        if (filteredTasks.isEmpty()) {
            taskState.setText("Không có công việc phù hợp.");
            return;
        }

        if (FILTER_PROJECT.equals(currentFilter)) {
            taskState.setText("Hiển thị " + filteredTasks.size() + " công việc theo dự án.");
        } else {
            taskState.setText("Hiển thị " + filteredTasks.size() + " công việc.");
        }

        for (Task task : filteredTasks) {
            View card = ViewFactory.taskCard(
                    this,
                    task.getTitle(),
                    task.getProjectName() + " - " + task.getDueDate(),
                    task.getStatus(),
                    badgeBackground(task.getStatus()),
                    badgeColor(task.getStatus())
            );
            card.setOnClickListener(v -> openTaskDetail(task));
            taskList.addView(card);
        }
    }

    private void setupProjectFilter() {
        Map<String, String> projectMap = new LinkedHashMap<>();
        projectMap.put("", "Tất cả dự án");
        for (Task task : allTasks) {
            String projectId = task.getProjectId();
            String projectName = task.getProjectName();
            if (projectId != null && !projectId.trim().isEmpty()
                    && projectName != null && !projectName.trim().isEmpty()) {
                projectMap.put(projectId, projectName);
            }
        }

        projectFilterIds.clear();
        projectFilterIds.addAll(projectMap.keySet());
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_dropdown_item,
                new ArrayList<>(projectMap.values())
        );
        projectFilterSpinner.setAdapter(adapter);
        selectedProjectId = projectFilterIds.isEmpty() ? "" : projectFilterIds.get(0);
    }

    private List<Task> filterTasks() {
        List<Task> filteredTasks = new ArrayList<>();
        for (Task task : allTasks) {
            if (!matchesFilter(task)) {
                continue;
            }
            if (!matchesKeyword(task)) {
                continue;
            }
            filteredTasks.add(task);
        }
        return filteredTasks;
    }

    private boolean matchesFilter(Task task) {
        if (FILTER_MINE.equals(currentFilter)) {
            return currentUserId.equals(task.getAssigneeId());
        }
        if (FILTER_ASSIGNED.equals(currentFilter)) {
            return currentUserId.equals(task.getCreatorId()) && !currentUserId.equals(task.getAssigneeId());
        }
        if (FILTER_PROJECT.equals(currentFilter)) {
            if (selectedProjectId == null || selectedProjectId.isEmpty()) {
                return task.getProjectId() != null && !task.getProjectId().trim().isEmpty();
            }
            return selectedProjectId.equals(task.getProjectId());
        }
        return true;
    }

    private boolean matchesKeyword(Task task) {
        if (currentKeyword.isEmpty()) {
            return true;
        }
        return contains(task.getTitle(), currentKeyword)
                || contains(task.getProjectName(), currentKeyword)
                || contains(task.getAssigneeName(), currentKeyword)
                || contains(task.getStatus(), currentKeyword);
    }

    private boolean contains(String value, String keyword) {
        return value != null && value.toLowerCase().contains(keyword);
    }

    private void openTaskDetail(Task task) {
        Intent intent = new Intent(this, TaskDetailActivity.class);
        intent.putExtra(TaskDetailActivity.EXTRA_TASK_ID, task.getId());
        startActivity(intent);
    }

    private int badgeBackground(String status) {
        if (Task.STATUS_DONE.equals(status)) {
            return R.drawable.bg_badge_green;
        }
        if (Task.STATUS_IN_PROGRESS.equals(status)) {
            return R.drawable.bg_badge_yellow;
        }
        return R.drawable.bg_badge_blue;
    }

    private int badgeColor(String status) {
        if (Task.STATUS_DONE.equals(status)) {
            return Color.rgb(33, 181, 127);
        }
        if (Task.STATUS_IN_PROGRESS.equals(status)) {
            return Color.rgb(239, 173, 68);
        }
        return Color.rgb(34, 197, 94);
    }

}
