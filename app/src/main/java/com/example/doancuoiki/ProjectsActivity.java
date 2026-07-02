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
import android.widget.ImageView;
import android.widget.TextView;

import com.example.doancuoiki.model.Project;
import com.example.doancuoiki.model.Task;
import com.example.doancuoiki.repository.ProjectRepository;
import com.example.doancuoiki.repository.TaskRepository;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ProjectsActivity extends Activity {
    public static final String EXTRA_PROJECT_ID = "projectId";

    private static final String GUEST_USER_ID = "guest";
    private static final String FILTER_ALL = "all";
    private static final String FILTER_OWNED = "owned";
    private static final String FILTER_DONE = "done";

    private final ProjectRepository projectRepository = new ProjectRepository();
    private final TaskRepository taskRepository = new TaskRepository();
    private final List<Project> allProjects = new ArrayList<>();
    private final Map<String, int[]> projectTaskStats = new HashMap<>();

    private LinearLayout projectList;
    private TextView projectState;
    private EditText searchInput;
    private TextView tabAll;
    private TextView tabOwned;
    private TextView tabDone;
    private String currentKeyword = "";
    private String currentUserId = GUEST_USER_ID;
    private String currentFilter = FILTER_ALL;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_projects);
        NavigationUtils.setupBottomNav(this, NavigationUtils.PROJECTS);

        View mainLayout = findViewById(android.R.id.content);
        if (mainLayout != null) {
            ViewCompat.setOnApplyWindowInsetsListener(mainLayout, (v, windowInsets) -> {
                Insets insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars());
                v.setPadding(v.getPaddingLeft(), insets.top, v.getPaddingRight(), 0);
                return windowInsets;
            });
        }

        bindViews();
        setupActions();
        resolveCurrentUser();
        loadProjects();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (projectList != null) {
            loadProjects();
        }
    }

    private View tabAllUnderline, tabOwnedUnderline, tabDoneUnderline;

    private void bindViews() {
        projectList = findViewById(R.id.projectList);
        projectState = findViewById(R.id.txtProjectState);
        searchInput = findViewById(R.id.edtSearchProject);
        tabAll = findViewById(R.id.tabAllProjects);
        tabOwned = findViewById(R.id.tabOwnedProjects);
        tabDone = findViewById(R.id.tabDoneProjects);
        
        tabAllUnderline = findViewById(R.id.tabAllUnderline);
        tabOwnedUnderline = findViewById(R.id.tabOwnedUnderline);
        tabDoneUnderline = findViewById(R.id.tabDoneUnderline);
    }

    private void setupActions() {
        findViewById(R.id.btnOpenAddProject).setOnClickListener(v ->
                NavigationUtils.open(this, AddProjectActivity.class));
        
        ImageView btnTopAdd = findViewById(R.id.btnTopAddProject);
        if (btnTopAdd != null) {
            btnTopAdd.setOnClickListener(v -> NavigationUtils.open(this, AddProjectActivity.class));
        }

        View.OnClickListener allListener = v -> setFilter(FILTER_ALL);
        View.OnClickListener ownedListener = v -> setFilter(FILTER_OWNED);
        View.OnClickListener doneListener = v -> setFilter(FILTER_DONE);

        tabAll.setOnClickListener(allListener);
        tabOwned.setOnClickListener(ownedListener);
        tabDone.setOnClickListener(doneListener);
        
        // Also make parent layouts clickable
        ((View)tabAll.getParent()).setOnClickListener(allListener);
        ((View)tabOwned.getParent()).setOnClickListener(ownedListener);
        ((View)tabDone.getParent()).setOnClickListener(doneListener);

        updateTabs();

        searchInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                currentKeyword = s.toString().trim().toLowerCase();
                renderProjects();
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
    }

    private void setFilter(String filter) {
        currentFilter = filter;
        updateTabs();
        renderProjects();
    }

    private void updateTabs() {
        styleTab(tabAll, FILTER_ALL.equals(currentFilter), tabAllUnderline);
        styleTab(tabOwned, FILTER_OWNED.equals(currentFilter), tabOwnedUnderline);
        styleTab(tabDone, FILTER_DONE.equals(currentFilter), tabDoneUnderline);
    }

    private void styleTab(TextView tab, boolean selected, View underline) {
        if (selected) {
            tab.setTextColor(Color.parseColor("#15B759"));
            tab.setTypeface(null, Typeface.BOLD);
            tab.setBackgroundResource(R.drawable.bg_nav_active);
            tab.setBackgroundTintList(android.content.res.ColorStateList.valueOf(Color.parseColor("#E6F9ED")));
            if (underline != null) underline.setVisibility(View.VISIBLE);
        } else {
            tab.setTextColor(Color.parseColor("#7D8496"));
            tab.setTypeface(null, Typeface.NORMAL);
            tab.setBackgroundResource(0);
            if (underline != null) underline.setVisibility(View.INVISIBLE);
        }
    }

    private void resolveCurrentUser() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            currentUserId = user.getUid();
        }
    }

    private void loadProjects() {
        projectState.setVisibility(View.VISIBLE);
        projectState.setText("Đang tải dự án...");
        projectRepository.getProjectsByUser(currentUserId, new ProjectRepository.ProjectListCallback() {
            @Override
            public void onSuccess(List<Project> projects) {
                allProjects.clear();
                projectTaskStats.clear();
                allProjects.addAll(projects);
                projectState.setText(projects.isEmpty()
                        ? "Chưa có dự án nào. Hãy tạo dự án đầu tiên."
                        : "Đã tải " + projects.size() + " dự án từ Firestore.");
                renderProjects();
                loadTaskStatsForProjects(projects);
            }

            @Override
            public void onError(Exception exception) {
                allProjects.clear();
                projectTaskStats.clear();
                projectState.setText("Không tải được dự án từ Firestore.");
                renderProjects();
            }
        });
    }

    private void loadTaskStatsForProjects(List<Project> projects) {
        for (Project project : projects) {
            if (project.getId() == null) {
                continue;
            }
            taskRepository.getTasksByProject(project.getId(), new TaskRepository.TaskListCallback() {
                @Override
                public void onSuccess(List<Task> tasks) {
                    projectTaskStats.put(project.getId(), new int[]{countDone(tasks), tasks.size()});
                    renderProjects();
                }

                @Override
                public void onError(Exception exception) {
                    projectTaskStats.put(project.getId(), new int[]{0, 0});
                    renderProjects();
                }
            });
        }
    }

    private void renderProjects() {
        projectList.removeAllViews();

        List<Project> filtered = filterProjects();
        if (filtered.isEmpty()) {
            projectState.setVisibility(View.VISIBLE);
            if (!currentKeyword.isEmpty()) {
                projectState.setText("Không có dự án phù hợp.");
            } else if (FILTER_OWNED.equals(currentFilter)) {
                projectState.setText("Bạn chưa quản lý dự án nào.");
            } else if (FILTER_DONE.equals(currentFilter)) {
                projectState.setText("Chưa có dự án hoàn thành.");
            } else {
                projectState.setText("Chưa có dự án nào. Hãy tạo dự án đầu tiên.");
            }
            return;
        }

        projectState.setVisibility(View.GONE);

        for (Project project : filtered) {
            int[] stats = projectTaskStats.get(project.getId());
            int done = stats == null ? 0 : stats[0];
            int total = stats == null ? 0 : stats[1];
            projectList.addView(ViewFactory.projectCard(
                    this,
                    valueOrDefault(project.getName(), "Dự án"),
                    valueOrDefault(project.getDescription(), "Chưa có mô tả"),
                    valueOrDefault(project.getEndDate(), "--"),
                    done + "/" + total,
                    statusForProject(project, total),
                    project.getProgress(),
                    memberLabels(project),
                    v -> openProjectDetail(project)
            ));
        }
    }

    private List<Project> filterProjects() {
        List<Project> filtered = new ArrayList<>();
        for (Project project : allProjects) {
            if (!matchesKeyword(project)) {
                continue;
            }
            if (FILTER_OWNED.equals(currentFilter) && !currentUserId.equals(project.getOwnerId())) {
                continue;
            }
            if (FILTER_DONE.equals(currentFilter) && project.getProgress() < 100) {
                continue;
            }
            filtered.add(project);
        }
        return filtered;
    }

    private boolean matchesKeyword(Project project) {
        return currentKeyword.isEmpty()
                || contains(project.getName(), currentKeyword)
                || contains(project.getDescription(), currentKeyword);
    }

    private boolean contains(String value, String keyword) {
        return value != null && value.toLowerCase().contains(keyword);
    }

    private int countDone(List<Task> tasks) {
        int count = 0;
        for (Task task : tasks) {
            if (Task.STATUS_DONE.equals(task.getStatus())) {
                count++;
            }
        }
        return count;
    }

    private String statusForProject(Project project, int totalTasks) {
        if (project.getProgress() >= 100) {
            return "Đã hoàn thành";
        }
        if (totalTasks == 0) {
            return "Tạm dừng";
        }
        return "Đang chạy";
    }

    private List<String> memberLabels(Project project) {
        List<String> labels = new ArrayList<>();
        List<String> members = project.getMembers();
        if (members == null) {
            return labels;
        }
        for (int i = 0; i < members.size(); i++) {
            labels.add(members.get(i).equals(project.getOwnerId()) ? "Chủ dự án" : "TV " + (i + 1));
        }
        return labels;
    }

    private void openProjectDetail(Project project) {
        Intent intent = new Intent(this, ProjectDetailActivity.class);
        intent.putExtra(EXTRA_PROJECT_ID, project.getId());
        intent.putExtra("projectName", project.getName());
        startActivity(intent);
    }

    private String valueOrDefault(String value, String defaultValue) {
        return value == null || value.trim().isEmpty() ? defaultValue : value.trim();
    }
}
