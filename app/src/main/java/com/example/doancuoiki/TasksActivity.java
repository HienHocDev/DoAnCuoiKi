package com.example.doancuoiki;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
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
    private String currentCategoryFilter = "Tất cả danh mục";
    private android.widget.ImageView btnFilter;

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

    private View tabAllUnderline, tabInProgressUnderline, tabPendingUnderline, tabDoneUnderline, tabCancelledUnderline;
    private TextView tabInProgress, tabPending, tabCancelled;

    private void bindViews() {
        taskList = findViewById(R.id.taskList);
        taskState = findViewById(R.id.txtTaskState);
        searchInput = findViewById(R.id.edtSearchTask);
        btnFilter = findViewById(R.id.btnFilter);
        tabAll = findViewById(R.id.tabAll);
        tabInProgress = findViewById(R.id.tabInProgress);
        tabPending = findViewById(R.id.tabPending);
        tabDone = findViewById(R.id.tabDone);
        tabCancelled = findViewById(R.id.tabCancelled);
        
        tabAllUnderline = findViewById(R.id.tabAllUnderline);
        tabInProgressUnderline = findViewById(R.id.tabInProgressUnderline);
        tabPendingUnderline = findViewById(R.id.tabPendingUnderline);
        tabDoneUnderline = findViewById(R.id.tabDoneUnderline);
        tabCancelledUnderline = findViewById(R.id.tabCancelledUnderline);
    }

    private void setupActions() {
        View mainLayout = findViewById(android.R.id.content);
        if (mainLayout != null) {
            ViewCompat.setOnApplyWindowInsetsListener(mainLayout, (v, windowInsets) -> {
                Insets insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars());
                v.setPadding(v.getPaddingLeft(), insets.top, v.getPaddingRight(), 0);
                return windowInsets;
            });
        }

        View.OnClickListener allListener = v -> setFilter(FILTER_ALL);
        View.OnClickListener inProgressListener = v -> setFilter(Task.STATUS_IN_PROGRESS);
        View.OnClickListener pendingListener = v -> setFilter(Task.STATUS_PENDING);
        View.OnClickListener doneListener = v -> setFilter(Task.STATUS_DONE);
        View.OnClickListener cancelledListener = v -> setFilter(Task.STATUS_CANCELLED);

        tabAll.setOnClickListener(allListener);
        tabInProgress.setOnClickListener(inProgressListener);
        tabPending.setOnClickListener(pendingListener);
        tabDone.setOnClickListener(doneListener);
        tabCancelled.setOnClickListener(cancelledListener);
        
        if (tabAll.getParent() instanceof View) ((View)tabAll.getParent()).setOnClickListener(allListener);
        if (tabInProgress.getParent() instanceof View) ((View)tabInProgress.getParent()).setOnClickListener(inProgressListener);
        if (tabPending.getParent() instanceof View) ((View)tabPending.getParent()).setOnClickListener(pendingListener);
        if (tabDone.getParent() instanceof View) ((View)tabDone.getParent()).setOnClickListener(doneListener);
        if (tabCancelled.getParent() instanceof View) ((View)tabCancelled.getParent()).setOnClickListener(cancelledListener);

        if (btnFilter != null) {
            btnFilter.setOnClickListener(v -> showCategoryFilterMenu(v));
        }

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
            public void afterTextChanged(android.text.Editable s) {
            }
        });
    }

    private void showCategoryFilterMenu(View v) {
        android.widget.PopupMenu popup = new android.widget.PopupMenu(this, v);
        popup.getMenu().add(0, 1, 0, "Tất cả danh mục");
        popup.getMenu().add(0, 2, 1, "Công việc");
        popup.getMenu().add(0, 3, 2, "Học tập");
        popup.getMenu().add(0, 4, 3, "Cá nhân");
        
        popup.setOnMenuItemClickListener(item -> {
            currentCategoryFilter = item.getTitle().toString();
            renderTasks();
            return true;
        });
        popup.show();
    }

    private void setFilter(String filter) {
        currentFilter = filter;
        updateTabs();
        renderTasks();
    }

    private void updateTabs() {
        styleTab(tabAll, FILTER_ALL.equals(currentFilter), tabAllUnderline);
        styleTab(tabInProgress, Task.STATUS_IN_PROGRESS.equals(currentFilter), tabInProgressUnderline);
        styleTab(tabPending, Task.STATUS_PENDING.equals(currentFilter), tabPendingUnderline);
        styleTab(tabDone, Task.STATUS_DONE.equals(currentFilter), tabDoneUnderline);
        styleTab(tabCancelled, Task.STATUS_CANCELLED.equals(currentFilter), tabCancelledUnderline);
    }

    private void styleTab(TextView tab, boolean selected, View underline) {
        if (tab == null) return;
        if (selected) {
            tab.setTextColor(Color.parseColor("#15B759"));
            tab.setTypeface(null, Typeface.BOLD);
            if (underline != null) underline.setVisibility(View.VISIBLE);
        } else {
            tab.setTextColor(Color.parseColor("#7D8496"));
            tab.setTypeface(null, Typeface.NORMAL);
            if (underline != null) underline.setVisibility(View.INVISIBLE);
        }
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
            View card = ViewFactory.detailedTaskCard(
                    this,
                    task.getTitle(),
                    "Nhóm: " + valueOrDefault(task.getProjectName(), "Dự án"),
                    valueOrDefault(task.getDueDate(), "Chưa có hạn"),
                    valueOrDefault(task.getPriority(), "Trung bình"),
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
        if (Task.STATUS_IN_PROGRESS.equals(currentFilter)) {
            return "Không có công việc đang làm.";
        }
        if (Task.STATUS_PENDING.equals(currentFilter)) {
            return "Không có công việc đang chờ.";
        }
        if (Task.STATUS_DONE.equals(currentFilter)) {
            return "Không có công việc đã xong.";
        }
        if (Task.STATUS_CANCELLED.equals(currentFilter)) {
            return "Không có công việc đã hủy.";
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
        if (!"Tất cả danh mục".equals(currentCategoryFilter)) {
            String cat = task.getCategory();
            if (cat == null) cat = "Chưa có";
            if (!currentCategoryFilter.equals(cat)) {
                return false;
            }
        }

        if (FILTER_ALL.equals(currentFilter)) {
            return true;
        }
        if (Task.STATUS_IN_PROGRESS.equals(currentFilter)) {
            return Task.STATUS_IN_PROGRESS.equals(task.getStatus());
        }
        if (Task.STATUS_PENDING.equals(currentFilter)) {
            return Task.STATUS_PENDING.equals(task.getStatus());
        }
        if (Task.STATUS_DONE.equals(currentFilter)) {
            return Task.STATUS_DONE.equals(task.getStatus());
        }
        if (Task.STATUS_CANCELLED.equals(currentFilter)) {
            return Task.STATUS_CANCELLED.equals(task.getStatus());
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
        if (Task.STATUS_CANCELLED.equals(status)) {
            return Color.rgb(239, 68, 68);
        }
        return Color.rgb(34, 197, 94);
    }

    private String valueOrDefault(String value, String defaultValue) {
        return value == null || value.trim().isEmpty() ? defaultValue : value.trim();
    }
}
