package com.example.doancuoiki;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.doancuoiki.model.Project;
import com.example.doancuoiki.repository.ProjectRepository;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.List;

public class HomeActivity extends Activity {
    private static final String GUEST_USER_ID = "guest";

    private final ProjectRepository projectRepository = new ProjectRepository();

    private LinearLayout projectList;
    private TextView totalProjectsText;
    private TextView totalTasksText;
    private TextView doneTasksText;
    private String currentUserId = GUEST_USER_ID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        NavigationUtils.setupBottomNav(this, NavigationUtils.HOME);

        projectList = findViewById(R.id.homeProjectList);
        totalProjectsText = findViewById(R.id.txtTotalProjects);
        totalTasksText = findViewById(R.id.txtTotalTasks);
        doneTasksText = findViewById(R.id.txtDoneTasks);

        resolveCurrentUser();
        findViewById(R.id.btnAddProject).setOnClickListener(v ->
                NavigationUtils.open(this, AddProjectActivity.class));
        loadProjects();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (projectList != null) {
            loadProjects();
        }
    }

    private void resolveCurrentUser() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            currentUserId = user.getUid();
        }
    }

    private void loadProjects() {
        projectRepository.getProjectsByUser(currentUserId, new ProjectRepository.ProjectListCallback() {
            @Override
            public void onSuccess(List<Project> projects) {
                renderProjects(projects);
            }

            @Override
            public void onError(Exception exception) {
                renderProjects(java.util.Collections.emptyList());
            }
        });
    }

    private void renderProjects(List<Project> projects) {
        projectList.removeAllViews();

        totalProjectsText.setText(projects.size() + "\nDự án");
        totalTasksText.setText("--\nCông việc");
        doneTasksText.setText("--\nHoàn thành");

        if (projects.isEmpty()) {
            projectList.addView(ViewFactory.notificationCard(
                    this,
                    "Chưa có dự án",
                    "Tạo dự án đầu tiên để bắt đầu quản lý công việc nhóm.",
                    ""
            ));
            return;
        }

        for (Project project : projects) {
            projectList.addView(ViewFactory.projectCard(
                    this,
                    project.getName(),
                    "Tiến độ " + project.getProgress() + "%",
                    project.getProgress(),
                    v -> openProjectDetail(project)
            ));
        }
    }

    private void openProjectDetail(Project project) {
        Intent intent = new Intent(this, ProjectDetailActivity.class);
        intent.putExtra(ProjectsActivity.EXTRA_PROJECT_ID, project.getId());
        intent.putExtra("projectName", project.getName());
        startActivity(intent);
    }
}
