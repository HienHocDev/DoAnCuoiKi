package com.example.doancuoiki;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
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
    private static final String FILTER_TODO = "todo";
    private static final String FILTER_DOING = "doing";
    private static final String FILTER_DONE = "done";

    private final TaskRepository taskRepository = new TaskRepository();
    private final List<Task> allTasks = new ArrayList<>();

    private LinearLayout taskList;
    private TextView taskState;
    private EditText searchInput;
    private TextView tabAll;
    private TextView tabTodo;
    private TextView tabDoing;
    private TextView tabDone;
    private String currentKeyword = "";
    private String currentUserId = "";
    private String currentFilter = FILTER_ALL;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tasks);
        NavigationUtils.setupBottomNav(this, NavigationUtils.TASKS);

        bindViews();
        setupActions();

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        currentUserId = user == null ? "" : user.getUid();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadTasks();
    }

    private void bindViews() {
        taskList = findViewById(R.id.taskList);
        taskState = findViewById(R.id.txtTaskState);
        searchInput = findViewById(R.id.edtSearchTask);
        tabAll = findViewById(R.id.tabAllTasks);
        tabTodo = findViewById(R.id.tabTodoTasks);
        tabDoing = findViewById(R.id.tabDoingTasks);
        tabDone = findViewById(R.id.tabDoneTasks);
    }

    private void setupActions() {
        tabAll.setOnClickListener(v -> setFilter(FILTER_ALL));
        tabTodo.setOnClickListener(v -> setFilter(FILTER_TODO));
        tabDoing.setOnClickListener(v -> setFilter(FILTER_DOING));
        tabDone.setOnClickListener(v -> setFilter(FILTER_DONE));
        updateTabs();

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

    private void setFilter(String filter) {
        currentFilter = filter;
        updateTabs();
        renderTasks();
    }

    private void updateTabs() {
        styleTab(tabAll, FILTER_ALL.equals(currentFilter));
        styleTab(tabTodo, FILTER_TODO.equals(currentFilter));
        styleTab(tabDoing, FILTER_DOING.equals(currentFilter));
        styleTab(tabDone, FILTER_DONE.equals(currentFilter));
    }

    private void styleTab(TextView tab, boolean selected) {
        tab.setTextColor(selected ? Color.rgb(34, 197, 94) : Color.rgb(125, 132, 150));
        tab.setTypeface(null, selected ? Typeface.BOLD : Typeface.NORMAL);
    }

    private void loadTasks() {
        if (currentUserId.isEmpty()) {
            taskState.setText("Bạn cần đăng nhập để xem công việc.");
            return;
        }
        taskState.setText("Đang tải công việc được giao...");
        taskRepository.getAssignedTasks(currentUserId, new TaskRepository.TaskListCallback() {
            @Override
            public void onSuccess(List<Task> tasks) {
                allTasks.clear();
                allTasks.addAll(tasks);
                renderTasks();
            }

            @Override
            public void onError(Exception exception) {
                allTasks.clear();
                taskState.setText("Không tải được công việc từ Firestore.");
                taskList.removeAllViews();
            }
        });
    }

    private void renderTasks() {
        taskList.removeAllViews();
        List<Task> filteredTasks = new ArrayList<>();
        for (Task task : allTasks) {
            if (matchesKeyword(task) && matchesFilter(task)) {
                filteredTasks.add(task);
            }
        }

        if (filteredTasks.isEmpty()) {
            taskState.setText(emptyText());
            return;
        }

        taskState.setText("Hiển thị " + filteredTasks.size() + " công việc của bạn.");
        for (Task task : filteredTasks) {
            View card = ViewFactory.taskCard(
                    this,
                    task.getTitle(),
                    valueOrDefault(task.getProjectName(), "Dự án") + " - "
                            + valueOrDefault(task.getDueDate(), "Chưa có hạn"),
                    task.getStatus(),
                    badgeBackground(task.getStatus()),
                    badgeColor(task.getStatus())
            );
            card.setOnClickListener(v -> openTaskDetail(task));
            taskList.addView(card);
        }
    }

    private String emptyText() {
        if (!currentKeyword.isEmpty()) {
            return "Không có công việc phù hợp.";
        }
        if (FILTER_TODO.equals(currentFilter)) {
            return "Không có công việc chưa làm.";
        }
        if (FILTER_DOING.equals(currentFilter)) {
            return "Không có công việc đang làm.";
        }
        if (FILTER_DONE.equals(currentFilter)) {
            return "Không có công việc đã xong.";
        }
        return "Bạn chưa được giao công việc nào.";
    }

    private boolean matchesKeyword(Task task) {
        if (currentKeyword.isEmpty()) {
            return true;
        }
        return contains(task.getTitle())
                || contains(task.getProjectName())
                || contains(task.getStatus());
    }

    private boolean matchesFilter(Task task) {
        if (FILTER_TODO.equals(currentFilter)) {
            return Task.STATUS_NOT_STARTED.equals(task.getStatus());
        }
        if (FILTER_DOING.equals(currentFilter)) {
            return Task.STATUS_IN_PROGRESS.equals(task.getStatus());
        }
        if (FILTER_DONE.equals(currentFilter)) {
            return Task.STATUS_DONE.equals(task.getStatus());
        }
        return true;
    }

    private boolean contains(String value) {
        return value != null && value.toLowerCase().contains(currentKeyword);
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

    private String valueOrDefault(String value, String defaultValue) {
        return value == null || value.trim().isEmpty() ? defaultValue : value.trim();
    }
}
