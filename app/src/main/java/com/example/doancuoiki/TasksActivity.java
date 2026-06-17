package com.example.doancuoiki;

import android.app.Activity;
import android.app.AlertDialog;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.doancuoiki.model.Task;
import com.example.doancuoiki.repository.TaskRepository;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;
import java.util.List;

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

    private String currentFilter = FILTER_ALL;
    private String currentKeyword = "";
    private String currentUserId = GUEST_USER_ID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tasks);
        NavigationUtils.setupBottomNav(this, NavigationUtils.TASKS);

        bindViews();
        setupActions();
        resolveCurrentUser();
        loadTasks();
    }

    private void bindViews() {
        taskList = findViewById(R.id.taskList);
        taskState = findViewById(R.id.txtTaskState);
        searchInput = findViewById(R.id.edtSearchTask);
        tabAll = findViewById(R.id.tabAll);
        tabMine = findViewById(R.id.tabMine);
        tabAssigned = findViewById(R.id.tabAssigned);
        tabProject = findViewById(R.id.tabProject);
    }

    private void setupActions() {
        findViewById(R.id.btnAddTask).setOnClickListener(v ->
                NavigationUtils.open(this, AddTaskActivity.class));

        tabAll.setOnClickListener(v -> changeFilter(FILTER_ALL));
        tabMine.setOnClickListener(v -> changeFilter(FILTER_MINE));
        tabAssigned.setOnClickListener(v -> changeFilter(FILTER_ASSIGNED));
        tabProject.setOnClickListener(v -> changeFilter(FILTER_PROJECT));

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
        taskRepository.getAllTasks(new TaskRepository.TaskListCallback() {
            @Override
            public void onSuccess(List<Task> tasks) {
                allTasks.clear();
                if (tasks.isEmpty()) {
                    taskState.setText("Chưa có công việc nào. Hãy thêm công việc đầu tiên.");
                } else {
                    allTasks.addAll(tasks);
                    taskState.setText("Đã tải " + tasks.size() + " công việc từ Firestore.");
                }
                renderTasks();
            }

            @Override
            public void onError(Exception exception) {
                allTasks.clear();
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
    }

    private void setTabState(TextView tab, boolean selected) {
        tab.setTextColor(selected ? Color.rgb(93, 95, 239) : Color.rgb(125, 132, 150));
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
            taskState.setText("Hiển thị " + filteredTasks.size() + " công việc đã có dự án.");
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
            card.setOnClickListener(v -> showStatusDialog(task));
            taskList.addView(card);
        }
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
            return task.getProjectName() != null && !task.getProjectName().trim().isEmpty();
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

    private void showStatusDialog(Task task) {
        String[] statuses = {
                Task.STATUS_NOT_STARTED,
                Task.STATUS_IN_PROGRESS,
                Task.STATUS_DONE
        };

        new AlertDialog.Builder(this)
                .setTitle(task.getTitle())
                .setItems(statuses, (dialog, which) -> updateTaskStatus(task, statuses[which]))
                .setNegativeButton("Đóng", null)
                .show();
    }

    private void updateTaskStatus(Task task, String status) {
        task.setStatus(status);

        if (task.getId() == null) {
            renderTasks();
            return;
        }

        taskRepository.updateTaskStatus(task.getId(), status, new TaskRepository.SimpleCallback() {
            @Override
            public void onSuccess() {
                renderTasks();
            }

            @Override
            public void onError(Exception exception) {
                NavigationUtils.showMessage(TasksActivity.this, "Cập nhật trạng thái Firestore lỗi");
                renderTasks();
            }
        });
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
        return Color.rgb(93, 95, 239);
    }

}
