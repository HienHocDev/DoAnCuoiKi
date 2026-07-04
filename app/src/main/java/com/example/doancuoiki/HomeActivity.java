package com.example.doancuoiki;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.PopupMenu;
import android.widget.ProgressBar;
import android.widget.Toast;
import android.widget.EditText;
import android.text.InputType;

import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.doancuoiki.model.Project;
import com.example.doancuoiki.model.Task;
import com.example.doancuoiki.model.User;
import com.example.doancuoiki.repository.ProjectRepository;
import com.example.doancuoiki.repository.TaskRepository;
import com.example.doancuoiki.repository.UserRepository;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class HomeActivity extends Activity {
    private static final String GUEST_USER_ID = "guest";

    private final ProjectRepository projectRepository = new ProjectRepository();
    private final TaskRepository taskRepository = new TaskRepository();
    private final UserRepository userRepository = new UserRepository();

    private LinearLayout projectList;
    private LinearLayout todayTaskList;
    private LinearLayout activityFeed;
    private TextView txtTodayTasksHeader;
    private TextView homeUserNameText;
    private TextView homeAvatarText;
    private TextView projectCountText;
    private TextView totalProjectsText;
    private TextView totalTasksText;
    private TextView doneTasksText;
    private ProgressBar projectProgressBar;
    private FloatingActionButton fabQuickAction;
    private String currentUserId = GUEST_USER_ID;
    private String currentUserName = "Thành viên";
    private int currentProjectCount = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        NavigationUtils.setupBottomNav(this, NavigationUtils.HOME);

        android.view.View mainLayout = findViewById(android.R.id.content);
        if (mainLayout != null) {
            ViewCompat.setOnApplyWindowInsetsListener(mainLayout, (v, windowInsets) -> {
                Insets insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars());
                v.setPadding(v.getPaddingLeft(), insets.top, v.getPaddingRight(), v.getPaddingBottom());
                return windowInsets;
            });
        }

        projectList = findViewById(R.id.homeProjectList);
        todayTaskList = findViewById(R.id.homeTodayTaskList);
        activityFeed = findViewById(R.id.homeActivityFeed);
        txtTodayTasksHeader = findViewById(R.id.txtTodayTasksHeader);

        homeUserNameText = findViewById(R.id.txtHomeUserName);
        homeAvatarText = findViewById(R.id.txtHomeAvatar);
        projectCountText = findViewById(R.id.txtProjectCount);
        totalProjectsText = findViewById(R.id.txtTotalProjects);
        totalTasksText = findViewById(R.id.txtTotalTasks);
        doneTasksText = findViewById(R.id.txtDoneTasks);
        projectProgressBar = findViewById(R.id.projectProgressBar);
        fabQuickAction = findViewById(R.id.fabQuickAction);

        resolveCurrentUser();

        fabQuickAction.setOnClickListener(v -> showQuickActionMenu());

        loadProjects();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (projectList != null) {
            loadProjects();
        }
    }

    private void showQuickActionMenu() {
        PopupMenu popup = new PopupMenu(this, fabQuickAction);
        popup.getMenu().add(1, 1, 1, "Tạo Dự Án Mới");
        popup.getMenu().add(1, 2, 2, "Tạo Công Việc Nhanh");

        popup.setOnMenuItemClickListener(item -> {
            if (item.getItemId() == 1) {
                NavigationUtils.open(this, AddProjectActivity.class);
                return true;
            } else if (item.getItemId() == 2) {
                showQuickTaskDialog();
                return true;
            }
            return false;
        });
        popup.show();
    }

    private void showQuickTaskDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Tạo công việc hôm nay");

        // Tạo ô nhập liệu nhanh cấu hình chuẩn tiếng Việt
        final EditText input = new EditText(this);
        input.setHint("Nhập tiêu đề công việc...");
        input.setInputType(android.text.InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);
        input.setPadding(40, 30, 40, 30);
        builder.setView(input);

        builder.setPositiveButton("Thêm", (dialog, which) -> {
            String taskTitle = input.getText().toString().trim();
            if (taskTitle.isEmpty()) {
                Toast.makeText(HomeActivity.this, "Vui lòng nhập tên công việc", Toast.LENGTH_SHORT).show();
                return;
            }

            String todayStr = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());

            Task newTask = new Task();
            newTask.setTitle(taskTitle);
            newTask.setStatus(Task.STATUS_NOT_STARTED);
            newTask.setDueDate(todayStr);
            newTask.setCreatorId(currentUserId);
            newTask.setProjectId("");

            // 2. Đẩy lên TaskRepository
            taskRepository.addTask(newTask, new TaskRepository.SimpleCallback() {
                @Override
                public void onSuccess() {
                    Toast.makeText(HomeActivity.this, "Đã thêm việc hôm nay!", Toast.LENGTH_SHORT).show();

                    com.example.doancuoiki.model.ActivityLog log = new com.example.doancuoiki.model.ActivityLog();
                    log.setProjectId("");
                    log.setUserName(currentUserName);
                    log.setActionText("đã tạo việc nhanh");
                    log.setTargetName(taskTitle);
                    log.setType("task"); // loại công việc
                    log.setTimestamp(new com.google.firebase.Timestamp(new Date()));

                    projectRepository.addActivityLog(log, null);

                    loadProjects();
                }

                @Override
                public void onError(Exception e) {
                    Toast.makeText(HomeActivity.this, "Lỗi: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        });

        builder.setNegativeButton("Hủy", (dialog, which) -> dialog.cancel());
        builder.show();
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
                loadTaskSummaryAndTodayTasks();
            }

            @Override
            public void onError(Exception exception) {
                renderProjects(java.util.Collections.emptyList());
                loadTaskSummaryAndTodayTasks();
            }
        });
    }

    private void loadTaskSummaryAndTodayTasks() {
        taskRepository.getTasksForUser(currentUserId, new TaskRepository.TaskListCallback() {
            @Override
            public void onSuccess(List<Task> tasks) {
                int done = 0;
                List<Task> todayTasks = new ArrayList<>();
                String todayStr = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());

                for (Task task : tasks) {
                    if (Task.STATUS_DONE.equals(task.getStatus())) {
                        done++;
                    } else {
                        // Logic kiểm tra: Hạn hôm nay hoặc Đã quá hạn và chưa hoàn thành
                        String dueDate = task.getDueDate();
                        if (dueDate != null && (dueDate.equals(todayStr) || dueDate.compareTo(todayStr) < 0)) {
                            todayTasks.add(task);
                        }
                    }
                }

                totalProjectsText.setText(currentProjectCount + "\nDự án");
                totalTasksText.setText(tasks.size() + "\nCông việc");
                doneTasksText.setText(done + "\nHoàn thành");

                renderTodayTasks(todayTasks);
                loadRealActivityFeed();
            }

            @Override
            public void onError(Exception exception) {
                totalProjectsText.setText(currentProjectCount + "\nDự án");
                totalTasksText.setText("0\nCông việc");
                doneTasksText.setText("0\nHoàn thành");
                renderTodayTasks(new ArrayList<>());
                loadRealActivityFeed();
            }
        });
    }

    private void renderTodayTasks(List<Task> todayTasks) {
        todayTaskList.removeAllViews();
        txtTodayTasksHeader.setText("Việc hôm nay (" + todayTasks.size() + " việc)");

        if (todayTasks.isEmpty()) {
            todayTaskList.addView(ViewFactory.notificationCard(
                    this,
                    "Hôm nay thảnh thơi",
                    "Bạn không có công việc nào cần giải quyết trong ngày hôm nay.",
                    "Trống"
            ));
            return;
        }

        for (Task task : todayTasks) {
            // Sử dụng một hàm thông báo/thẻ checklist gọn nhẹ từ ViewFactory
            todayTaskList.addView(ViewFactory.notificationCard(
                    this,
                    "[ ] " + task.getTitle(),
                    "Hạn chót: " + valueOrDefault(task.getDueDate(), "--"),
                    "Task"
            ));
        }
    }

    private void loadRealActivityFeed() {
        // Gọi hàm từ repository để lấy danh sách hoạt động thật của user
        projectRepository.getRecentActivities(currentUserId, new ProjectRepository.ActivityListCallback() {
            @Override
            public void onSuccess(List<com.example.doancuoiki.model.ActivityLog> logs) {
                renderActivityFeed(logs);
            }

            @Override
            public void onError(Exception exception) {
                // Nếu lỗi hoặc chưa có hoạt động nào thì báo trống
                activityFeed.removeAllViews();
                activityFeed.addView(ViewFactory.notificationCard(
                        HomeActivity.this,
                        "Chưa có hoạt động mới",
                        "Mọi người chưa có cập nhật nào gần đây.",
                        "Trống"
                ));
            }
        });
    }

    private void renderActivityFeed(List<com.example.doancuoiki.model.ActivityLog> logs) {
        activityFeed.removeAllViews();

        if (logs == null || logs.isEmpty()) {
            activityFeed.addView(ViewFactory.notificationCard(
                    this,
                    "Chưa có hoạt động mới",
                    "Mọi người chưa có cập nhật nào gần đây.",
                    "Trống"
            ));
            return;
        }

        // Hiển thị tối đa 2-3 hoạt động mới nhất để tránh làm rối trang chủ
        int limit = Math.min(logs.size(), 3);
        for (int i = 0; i < limit; i++) {
            com.example.doancuoiki.model.ActivityLog log = logs.get(i);

            if ("comment".equalsIgnoreCase(log.getType())) {
                continue;
            }

            String uName = valueOrDefault(log.getUserName(), "Thành viên");
            String action = valueOrDefault(log.getActionText(), "đã cập nhật");
            String target = log.getTargetName();

            String icon = "✅ ";
            if ("project".equalsIgnoreCase(log.getType())) {
                icon = "📁 ";
            } else if ("task".equalsIgnoreCase(log.getType())) {
                icon = "📝 ";
            }

            // Logic kiểm tra targetName để đóng mở ngoặc nháy kép thông minh
            String title;
            if (target != null && !target.trim().isEmpty()) {
                title = icon + uName + " " + action + " \"" + target.trim() + "\"";
            } else {
                title = icon + uName + " " + action;
            }

            String timeAgo = valueOrDefault(log.getTimeAgo(), "Vừa xong");

            activityFeed.addView(ViewFactory.notificationCard(
                    this,
                    title,
                    timeAgo,
                    "Activity"
            ));
        }
    }

    private void renderProjects(List<Project> projects) {
        projectList.removeAllViews();
        currentProjectCount = projects.size();

        projectCountText.setText(projects.size() + " dự án");

        if (projects.isEmpty()) {
            if (projectProgressBar != null) {
                projectProgressBar.setProgress(0);
            }
            projectList.addView(ViewFactory.notificationCard(
                    this,
                    "Chưa có dự án",
                    "Tạo dự án đầu tiên để bắt đầu quản lý công việc nhóm.",
                    "Trống"
            ));
            return;
        }

        int totalProgress = 0;
        for (Project project : projects) {
            totalProgress += project.getProgress();


            projectList.addView(ViewFactory.homeProjectCard(
                    this,
                    project.getName(),
                    valueOrDefault(project.getDescription(), "Từ " + valueOrDefault(project.getStartDate(), "--") + " đến " + valueOrDefault(project.getEndDate(), "--")),
                    project.getProgress(),
                    v -> openProjectDetail(project)
            ));
        }

        if (projectProgressBar != null) {
            projectProgressBar.setProgress(totalProgress / projects.size());
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
