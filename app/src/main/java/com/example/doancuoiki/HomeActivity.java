package com.example.doancuoiki;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.doancuoiki.model.Project;
import com.example.doancuoiki.model.Task;
import com.example.doancuoiki.model.User;
import com.example.doancuoiki.repository.ProjectRepository;
import com.example.doancuoiki.repository.TaskRepository;
import com.example.doancuoiki.repository.UserRepository;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.List;

public class HomeActivity extends Activity {
    private static final String GUEST_USER_ID = "guest";

    private final ProjectRepository projectRepository = new ProjectRepository();
    private final TaskRepository taskRepository = new TaskRepository();
    private final UserRepository userRepository = new UserRepository();

    private LinearLayout projectList;
    private TextView homeUserNameText;
    private TextView homeAvatarText;
    private TextView projectCountText;
    private TextView totalProjectsText;
    private TextView totalTasksText;
    private TextView doneTasksText;
    private String currentUserId = GUEST_USER_ID;
    private int currentProjectCount = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        NavigationUtils.setupBottomNav(this, NavigationUtils.HOME);

        projectList = findViewById(R.id.homeProjectList);
        homeUserNameText = findViewById(R.id.txtHomeUserName);
        homeAvatarText = findViewById(R.id.txtHomeAvatar);
        projectCountText = findViewById(R.id.txtProjectCount);
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
            homeUserNameText.setText(user.getEmail() == null ? "TaskFlow" : user.getEmail());
            homeAvatarText.setText(initials(user.getEmail()));
            loadUserName(user);
        }
    }

    private void loadUserName(FirebaseUser firebaseUser) {
        userRepository.getUser(firebaseUser.getUid(), new UserRepository.UserCallback() {
            @Override
            public void onSuccess(User user) {
                String name = valueOrDefault(user.getName(), firebaseUser.getEmail());
                homeUserNameText.setText(name);
                homeAvatarText.setText(initials(name));
            }

            @Override
            public void onError(Exception exception) {
            }
        });
    }

    private void loadProjects() {
        projectRepository.getProjectsByUser(currentUserId, new ProjectRepository.ProjectListCallback() {
            @Override
            public void onSuccess(List<Project> projects) {
                renderProjects(projects);
                loadTaskSummary();
            }

            @Override
            public void onError(Exception exception) {
                renderProjects(java.util.Collections.emptyList());
                loadTaskSummary();
            }
        });
    }

    private void loadTaskSummary() {
        taskRepository.getTasksForUser(currentUserId, new TaskRepository.TaskListCallback() {
            @Override
            public void onSuccess(List<Task> tasks) {
                int done = 0;
                for (Task task : tasks) {
                    if (Task.STATUS_DONE.equals(task.getStatus())) {
                        done++;
                    }
                }
                totalProjectsText.setText(currentProjectCount + "\nDự án");
                totalTasksText.setText(tasks.size() + "\nCông việc");
                doneTasksText.setText(done + "\nHoàn thành");
            }

            @Override
            public void onError(Exception exception) {
                totalProjectsText.setText(currentProjectCount + "\nDự án");
                totalTasksText.setText("0\nCông việc");
                doneTasksText.setText("0\nHoàn thành");
            }
        });
    }

    private void renderProjects(List<Project> projects) {
        projectList.removeAllViews();
        currentProjectCount = projects.size();

        totalProjectsText.setText(projects.size() + "\nDự án");
        totalTasksText.setText("0\nCông việc");
        doneTasksText.setText("0\nHoàn thành");
        projectCountText.setText(projects.size() + " dự án");

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
            projectList.addView(ViewFactory.homeProjectCard(
                    this,
                    project.getName(),
                    valueOrDefault(project.getDescription(), "Từ " + valueOrDefault(project.getStartDate(), "--") + " đến " + valueOrDefault(project.getEndDate(), "--")),
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

    private String initials(String name) {
        String value = valueOrDefault(name, "TaskFlow").trim();
        if (value.contains("@")) {
            value = value.substring(0, value.indexOf("@"));
        }
        if (value.isEmpty()) {
            return "TF";
        }

        String[] parts = value.split("\\s+");
        if (parts.length == 1) {
            return parts[0].substring(0, 1).toUpperCase();
        }
        return (parts[0].substring(0, 1) + parts[parts.length - 1].substring(0, 1)).toUpperCase();
    }

    private String valueOrDefault(String value, String defaultValue) {
        if (value == null || value.trim().isEmpty()) {
            return defaultValue;
        }
        return value.trim();
    }
}
