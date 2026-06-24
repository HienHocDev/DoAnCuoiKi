package com.example.doancuoiki;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.doancuoiki.model.Project;
import com.example.doancuoiki.model.Task;
import com.example.doancuoiki.repository.ProjectRepository;
import com.example.doancuoiki.repository.TaskRepository;

import java.util.List;

public class ProjectDetailActivity extends Activity {
    private final ProjectRepository projectRepository = new ProjectRepository();
    private final TaskRepository taskRepository = new TaskRepository();

    private TextView titleText;
    private TextView descriptionText;
    private TextView dateText;
    private TextView totalTaskText;
    private TextView doneTaskText;
    private TextView progressText;
    private LinearLayout taskList;

    // Khai báo thêm 2 biến điều khiển nút bấm Sửa và Xóa
    private android.view.View btnEdit;
    private android.view.View btnDelete;

    private String projectId;
    private Project currentProject;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_project_detail);

        bindViews();
        projectId = getIntent().getStringExtra(ProjectsActivity.EXTRA_PROJECT_ID);

        // Cấu hình sự kiện cho các nút bấm
        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
        findViewById(R.id.btnOpenReport).setOnClickListener(v ->
                NavigationUtils.open(this, ReportActivity.class));

        // Gắn sự kiện click cho nút Sửa và Xóa dự án vừa thêm
        btnEdit.setOnClickListener(v -> openEditProject());
        btnDelete.setOnClickListener(v -> confirmDeleteProject());

        loadProject();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (taskList != null && currentProject != null) {
            loadProjectTasks();
        }
    }

    private void bindViews() {
        titleText = findViewById(R.id.txtProjectTitle);
        descriptionText = findViewById(R.id.txtProjectDescription);
        dateText = findViewById(R.id.txtProjectDate);
        totalTaskText = findViewById(R.id.txtProjectTaskCount);
        doneTaskText = findViewById(R.id.txtProjectDoneCount);
        progressText = findViewById(R.id.txtProjectProgress);
        taskList = findViewById(R.id.projectTaskList);

        // Ánh xạ id nút bấm từ layout XML
        btnEdit = findViewById(R.id.btnEditProject);
        btnDelete = findViewById(R.id.btnDeleteProject);
    }

    private void loadProject() {
        if (projectId == null || projectId.trim().isEmpty()) {
            renderMissingProject();
            return;
        }

        projectRepository.getProjectById(projectId, new ProjectRepository.ProjectCallback() {
            @Override
            public void onSuccess(Project project) {
                currentProject = project;
                renderProject(project);
                loadProjectTasks();
            }

            @Override
            public void onError(Exception exception) {
                NavigationUtils.showMessage(ProjectDetailActivity.this, "Không tải được dự án");
                renderMissingProject();
            }
        });
    }

    private void loadProjectTasks() {
        taskRepository.getTasksByProject(currentProject.getId(), new TaskRepository.TaskListCallback() {
            @Override
            public void onSuccess(List<Task> tasks) {
                renderTasks(tasks);
                updateProjectProgressIfNeeded(tasks);
            }

            @Override
            public void onError(Exception exception) {
                renderTasks(java.util.Collections.emptyList());
            }
        });
    }

    private void renderProject(Project project) {
        titleText.setText(valueOrDefault(project.getName(), "Chi tiết dự án"));
        descriptionText.setText(valueOrDefault(project.getDescription(), "Chưa có mô tả"));
        dateText.setText("Từ " + valueOrDefault(project.getStartDate(), "--") + " đến " + valueOrDefault(project.getEndDate(), "--"));
    }

    private void renderTasks(List<Task> tasks) {
        taskList.removeAllViews();

        int total = tasks.size();
        int done = countDone(tasks);
        int progress = percent(done, total);

        totalTaskText.setText(total + "\nTổng công việc");
        doneTaskText.setText(done + "\nHoàn thành");
        progressText.setText(progress + "%\nTiến độ");

        if (tasks.isEmpty()) {
            taskList.addView(ViewFactory.notificationCard(this, "Chưa có công việc", "Dự án này chưa có task nào", ""));
            return;
        }

        for (Task task : tasks) {
            android.view.View card = ViewFactory.taskCard(
                    this,
                    task.getTitle(),
                    valueOrDefault(task.getDueDate(), "Chưa có hạn") + " - " + valueOrDefault(task.getAssigneeName(), "Chưa phân công"),
                    task.getStatus(),
                    badgeBackground(task.getStatus()),
                    badgeColor(task.getStatus())
            );
            card.setOnClickListener(v -> openTaskDetail(task));
            taskList.addView(card);
        }
    }

    private void openTaskDetail(Task task) {
        Intent intent = new Intent(this, TaskDetailActivity.class);
        intent.putExtra(TaskDetailActivity.EXTRA_TASK_ID, task.getId());
        startActivity(intent);
    }

    // Hàm xử lý hiển thị hộp thoại xác nhận xóa dự án
    private void confirmDeleteProject() {
        if (currentProject == null) return;

        new android.app.AlertDialog.Builder(this)
                .setTitle("Xóa dự án")
                .setMessage("Bạn có chắc chắn muốn xóa dự án \"" + currentProject.getName() + "\" không? Toàn bộ các công việc bên trong cũng sẽ bị gỡ bỏ.")
                .setPositiveButton("Xóa", (dialog, which) -> {
                    projectRepository.deleteProject(projectId, new ProjectRepository.SimpleCallback() {
                        @Override
                        public void onSuccess() {
                            NavigationUtils.showMessage(ProjectDetailActivity.this, "Đã xóa dự án thành công");
                            finish(); // Đóng màn hình chi tiết, tự động quay trở lại danh sách
                        }

                        @Override
                        public void onError(Exception exception) {
                            NavigationUtils.showMessage(ProjectDetailActivity.this, "Xóa thất bại: " + exception.getMessage());
                        }
                    });
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    // Hàm xử lý đóng gói thông tin hiện tại và gửi sang AddProjectActivity để chỉnh sửa
    private void openEditProject() {
        if (currentProject == null) return;

        Intent intent = new Intent(this, AddProjectActivity.class);
        intent.putExtra("isEditMode", true);
        intent.putExtra(ProjectsActivity.EXTRA_PROJECT_ID, currentProject.getId());
        intent.putExtra("projectName", currentProject.getName());
        intent.putExtra("projectDesc", currentProject.getDescription());
        intent.putExtra("startDate", currentProject.getStartDate());
        intent.putExtra("endDate", currentProject.getEndDate());
        startActivity(intent);
    }

    private void updateProjectProgressIfNeeded(List<Task> tasks) {
        if (currentProject == null || currentProject.getId() == null) {
            return;
        }

        int progress = percent(countDone(tasks), tasks.size());
        if (progress == currentProject.getProgress()) {
            return;
        }

        currentProject.setProgress(progress);
        projectRepository.updateProgress(currentProject.getId(), progress, new ProjectRepository.SimpleCallback() {
            @Override
            public void onSuccess() {
            }

            @Override
            public void onError(Exception exception) {
            }
        });
    }

    private int countDone(List<Task> tasks) {
        int count = 0;
        for (Task task : tasks) {
            if ("Hoàn thành".equals(task.getStatus())) {
                count++;
            }
        }
        return count;
    }

    private int percent(int value, int total) {
        if (total == 0) {
            return 0;
        }
        return Math.round(value * 100f / total);
    }

    private int badgeBackground(String status) {
        if ("Hoàn thành".equals(status)) {
            return R.drawable.bg_badge_green;
        }
        if ("Đang làm".equals(status)) {
            return R.drawable.bg_badge_yellow;
        }
        return R.drawable.bg_badge_blue;
    }

    private int badgeColor(String status) {
        if ("Hoàn thành".equals(status)) {
            return Color.rgb(33, 181, 127);
        }
        if ("Đang làm".equals(status)) {
            return Color.rgb(239, 173, 68);
        }
        return Color.rgb(93, 95, 239);
    }

    private String valueOrDefault(String value, String defaultValue) {
        if (value == null || value.trim().isEmpty()) {
            return defaultValue;
        }
        return value.trim();
    }

    private void renderMissingProject() {
        currentProject = null;
        titleText.setText("Không tìm thấy dự án");
        descriptionText.setText("Dự án này chưa có dữ liệu trên Firestore hoặc đã bị xóa.");
        dateText.setText("Từ -- đến --");
        renderTasks(java.util.Collections.emptyList());
    }
}