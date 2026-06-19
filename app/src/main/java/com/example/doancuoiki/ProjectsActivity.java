package com.example.doancuoiki;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.doancuoiki.model.Project;
import com.example.doancuoiki.repository.ProjectRepository;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;
import java.util.List;

public class ProjectsActivity extends Activity {
    public static final String EXTRA_PROJECT_ID = "projectId";

    private static final String GUEST_USER_ID = "guest";

    private final ProjectRepository projectRepository = new ProjectRepository();
    private final List<Project> allProjects = new ArrayList<>();

    private LinearLayout projectList;
    private TextView projectState;
    private EditText searchInput;
    private String currentKeyword = "";
    private String currentUserId = GUEST_USER_ID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_projects);
        NavigationUtils.setupBottomNav(this, NavigationUtils.PROJECTS);

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

    private void bindViews() {
        projectList = findViewById(R.id.projectList);
        projectState = findViewById(R.id.txtProjectState);
        searchInput = findViewById(R.id.edtSearchProject);
    }

    private void setupActions() {
        findViewById(R.id.btnOpenAddProject).setOnClickListener(v ->
                NavigationUtils.open(this, AddProjectActivity.class));

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

    private void resolveCurrentUser() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            currentUserId = user.getUid();
        }
    }

    private void loadProjects() {
        projectState.setText("Đang tải dự án...");
        projectRepository.getProjectsByUser(currentUserId, new ProjectRepository.ProjectListCallback() {
            @Override
            public void onSuccess(List<Project> projects) {
                allProjects.clear();
                if (projects.isEmpty()) {
                    projectState.setText("Chưa có dự án nào. Hãy tạo dự án đầu tiên.");
                } else {
                    allProjects.addAll(projects);
                    projectState.setText("Đã tải " + projects.size() + " dự án từ Firestore.");
                }
                renderProjects();
            }

            @Override
            public void onError(Exception exception) {
                allProjects.clear();
                projectState.setText("Không tải được dự án từ Firestore.");
                renderProjects();
            }
        });
    }

    private void renderProjects() {
        projectList.removeAllViews();

        List<Project> filtered = filterProjects();
        if (filtered.isEmpty()) {
            projectState.setText("Không có dự án phù hợp.");
            return;
        }

        if (currentKeyword.isEmpty()) {
            projectState.setText("Hiển thị " + filtered.size() + " dự án.");
        } else {
            projectState.setText("Tìm thấy " + filtered.size() + " dự án.");
        }

        for (Project project : filtered) {
            String subtitle = project.getDescription();
            if (subtitle == null || subtitle.trim().isEmpty()) {
                subtitle = "Tiến độ " + project.getProgress() + "%";
            }

            projectList.addView(ViewFactory.projectCard(
                    this,
                    project.getName(),
                    subtitle,
                    project.getProgress(),
                    v -> openProjectDetail(project)
            ));
        }
    }

    private List<Project> filterProjects() {
        List<Project> filtered = new ArrayList<>();
        for (Project project : allProjects) {
            if (currentKeyword.isEmpty()
                    || contains(project.getName(), currentKeyword)
                    || contains(project.getDescription(), currentKeyword)) {
                filtered.add(project);
            }
        }
        return filtered;
    }

    private boolean contains(String value, String keyword) {
        return value != null && value.toLowerCase().contains(keyword);
    }

    private void openProjectDetail(Project project) {
        Intent intent = new Intent(this, ProjectDetailActivity.class);
        intent.putExtra(EXTRA_PROJECT_ID, project.getId());
        intent.putExtra("projectName", project.getName());
        startActivity(intent);
    }

}
