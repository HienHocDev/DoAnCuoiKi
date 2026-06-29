package com.example.doancuoiki;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ProjectDetailActivity extends Activity {
    private final ProjectRepository projectRepository = new ProjectRepository();
    private final TaskRepository taskRepository = new TaskRepository();
    private final UserRepository userRepository = new UserRepository();

    private TextView titleText;
    private TextView descriptionText;
    private TextView dateText;
    private TextView ownerNoticeText;
    private TextView totalTaskText;
    private TextView doneTaskText;
    private TextView progressText;
    private LinearLayout memberList;
    private LinearLayout taskList;
    private LinearLayout ownerActions;
    private View reportButton;
    private View deleteButton;

    private String projectId;
    private String currentUserId = "";
    private Project currentProject;
    private final List<User> displayedMembers = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_project_detail);

        bindViews();
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        currentUserId = user == null ? "" : user.getUid();
        projectId = getIntent().getStringExtra(ProjectsActivity.EXTRA_PROJECT_ID);

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
        findViewById(R.id.btnAddMember).setOnClickListener(v -> showAddMemberDialog());
        findViewById(R.id.btnAddProjectTask).setOnClickListener(v -> openAddTask());
        reportButton.setOnClickListener(v -> openAddTask());
        deleteButton.setOnClickListener(v -> confirmDeleteProject());

        loadProject();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (currentProject != null) {
            loadProjectTasks();
        }
    }

    private void bindViews() {
        titleText = findViewById(R.id.txtProjectTitle);
        descriptionText = findViewById(R.id.txtProjectDescription);
        dateText = findViewById(R.id.txtProjectDate);
        ownerNoticeText = findViewById(R.id.txtOwnerNotice);
        totalTaskText = findViewById(R.id.txtProjectTaskCount);
        doneTaskText = findViewById(R.id.txtProjectDoneCount);
        progressText = findViewById(R.id.txtProjectProgress);
        memberList = findViewById(R.id.projectMemberList);
        taskList = findViewById(R.id.projectTaskList);
        ownerActions = findViewById(R.id.ownerActions);
        reportButton = findViewById(R.id.btnOpenReport);
        deleteButton = findViewById(R.id.btnDeleteProject);
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
                renderProject();
                loadMembers();
                loadProjectTasks();
            }

            @Override
            public void onError(Exception exception) {
                NavigationUtils.showMessage(ProjectDetailActivity.this, "Không tải được dự án");
                renderMissingProject();
            }
        });
    }

    private void renderProject() {
        titleText.setText(valueOrDefault(currentProject.getName(), "Chi tiết dự án"));
        descriptionText.setText(valueOrDefault(currentProject.getDescription(), "Chưa có mô tả"));
        dateText.setText("Từ " + valueOrDefault(currentProject.getStartDate(), "--")
                + " đến " + valueOrDefault(currentProject.getEndDate(), "--"));

        boolean isOwner = isCurrentUserOwner();
        ownerActions.setVisibility(isOwner ? View.VISIBLE : View.GONE);
        reportButton.setVisibility(isOwner ? View.VISIBLE : View.GONE);
        deleteButton.setVisibility(isOwner ? View.VISIBLE : View.GONE);
        ownerNoticeText.setText(isOwner
                ? "Bạn là chủ dự án. Bạn có thể thêm thành viên và giao công việc cho nhóm."
                : "Bạn là thành viên dự án. Bạn chỉ có thể xem và cập nhật trạng thái việc được giao.");
    }

    private void loadMembers() {
        List<String> ids = currentProject.getMembers();
        if (ids == null) {
            ids = new ArrayList<>();
        }
        userRepository.getUsersByIds(ids, new UserRepository.UserListCallback() {
            @Override
            public void onSuccess(List<User> users) {
                renderMembers(users);
            }

            @Override
            public void onError(Exception exception) {
                renderMembers(Collections.emptyList());
            }
        });
    }

    private void renderMembers(List<User> users) {
        displayedMembers.clear();
        displayedMembers.addAll(users);
        memberList.removeAllViews();

        List<String> names = new ArrayList<>();
        for (User user : users) {
            names.add(valueOrDefault(user.getName(), valueOrDefault(user.getEmail(), "Thành viên")));
        }
        memberList.addView(ViewFactory.avatarStack(this, names), new LinearLayout.LayoutParams(0, -1, 1));

        TextView label = new TextView(this);
        label.setText(users.size() + " thành viên");
        label.setTextColor(Color.rgb(125, 132, 150));
        label.setTextSize(13);
        memberList.addView(label, new LinearLayout.LayoutParams(-2, -2));
    }

    private void showAddMemberDialog() {
        if (!isCurrentUserOwner()) {
            NavigationUtils.showMessage(this, "Chỉ chủ dự án được thêm thành viên");
            return;
        }

        EditText input = new EditText(this);
        input.setHint("Email hoặc UID tài khoản");
        input.setSingleLine(true);
        int padding = Math.round(20 * getResources().getDisplayMetrics().density);
        input.setPadding(padding, padding / 2, padding, padding / 2);

        new AlertDialog.Builder(this)
                .setTitle("Thêm thành viên")
                .setMessage("Thành viên phải đăng ký tài khoản trong ứng dụng.")
                .setView(input)
                .setNegativeButton("Hủy", null)
                .setPositiveButton("Thêm", (dialog, which) ->
                        findAndAddMember(input.getText().toString()))
                .show();
    }

    private void findAndAddMember(String emailOrUid) {
        userRepository.findUser(emailOrUid, new UserRepository.UserCallback() {
            @Override
            public void onSuccess(User user) {
                if (currentProject.getMembers() != null
                        && currentProject.getMembers().contains(user.getId())) {
                    NavigationUtils.showMessage(ProjectDetailActivity.this,
                            "Người dùng đã có trong dự án");
                    return;
                }
                projectRepository.addMember(projectId, user.getId(),
                        new ProjectRepository.SimpleCallback() {
                            @Override
                            public void onSuccess() {
                                if (currentProject.getMembers() == null) {
                                    currentProject.setMembers(new ArrayList<>());
                                }
                                if (!currentProject.getMembers().contains(user.getId())) {
                                    currentProject.getMembers().add(user.getId());
                                }
                                if (!containsDisplayedMember(user.getId())) {
                                    displayedMembers.add(user);
                                }
                                renderMembers(new ArrayList<>(displayedMembers));
                                NavigationUtils.showMessage(ProjectDetailActivity.this,
                                        "Đã thêm thành viên");
                            }

                            @Override
                            public void onError(Exception exception) {
                                NavigationUtils.showMessage(ProjectDetailActivity.this,
                                        "Không thêm được thành viên");
                            }
                        });
            }

            @Override
            public void onError(Exception exception) {
                NavigationUtils.showMessage(ProjectDetailActivity.this,
                        valueOrDefault(exception.getMessage(), "Không tìm thấy tài khoản"));
            }
        });
    }

    private boolean containsDisplayedMember(String userId) {
        for (User member : displayedMembers) {
            if (userId.equals(member.getId())) {
                return true;
            }
        }
        return false;
    }

    private void openAddTask() {
        if (!isCurrentUserOwner()) {
            NavigationUtils.showMessage(this, "Chỉ chủ dự án được giao công việc");
            return;
        }
        Intent intent = new Intent(this, AddTaskActivity.class);
        intent.putExtra(AddTaskActivity.EXTRA_PROJECT_ID, projectId);
        startActivity(intent);
    }

    private void loadProjectTasks() {
        taskRepository.getTasksByProject(currentProject.getId(), new TaskRepository.TaskListCallback() {
            @Override
            public void onSuccess(List<Task> tasks) {
                renderTasks(visibleTasksForCurrentUser(tasks), tasks);
                updateProjectProgressIfNeeded(tasks);
            }

            @Override
            public void onError(Exception exception) {
                renderTasks(Collections.emptyList(), Collections.emptyList());
            }
        });
    }

    private void renderTasks(List<Task> visibleTasks, List<Task> allProjectTasks) {
        taskList.removeAllViews();
        int total = allProjectTasks.size();
        int done = countDone(allProjectTasks);
        int progress = percent(done, total);

        totalTaskText.setText(total + "\nTổng công việc");
        doneTaskText.setText(done + "\nHoàn thành");
        progressText.setText(progress + "%\nTiến độ");

        if (visibleTasks.isEmpty()) {
            String message = isCurrentUserOwner()
                    ? "Dự án chưa có công việc. Hãy thêm công việc đầu tiên cho nhóm."
                    : "Bạn chưa có công việc được giao trong dự án này.";
            taskList.addView(ViewFactory.notificationCard(this, "Chưa có công việc", message, ""));
            return;
        }

        for (Task task : visibleTasks) {
            View card = ViewFactory.taskCard(
                    this,
                    task.getTitle(),
                    valueOrDefault(task.getDueDate(), "Chưa có hạn") + " - "
                            + valueOrDefault(task.getAssigneeName(), "Chưa phân công"),
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

    private void confirmDeleteProject() {
        if (!isCurrentUserOwner()) {
            return;
        }
        new AlertDialog.Builder(this)
                .setTitle("Xóa dự án")
                .setMessage("Bạn có chắc muốn xóa dự án này không?")
                .setNegativeButton("Hủy", null)
                .setPositiveButton("Xóa", (dialog, which) ->
                        projectRepository.deleteProject(projectId,
                                new ProjectRepository.SimpleCallback() {
                                    @Override
                                    public void onSuccess() {
                                        NavigationUtils.showMessage(ProjectDetailActivity.this,
                                                "Đã xóa dự án");
                                        finish();
                                    }

                                    @Override
                                    public void onError(Exception exception) {
                                        NavigationUtils.showMessage(ProjectDetailActivity.this,
                                                "Xóa dự án thất bại");
                                    }
                                }))
                .show();
    }

    private void updateProjectProgressIfNeeded(List<Task> tasks) {
        if (currentProject == null || currentProject.getId() == null || !isCurrentUserOwner()) {
            return;
        }
        int progress = percent(countDone(tasks), tasks.size());
        if (progress == currentProject.getProgress()) {
            return;
        }
        currentProject.setProgress(progress);
        projectRepository.updateProgress(currentProject.getId(), progress,
                new ProjectRepository.SimpleCallback() {
                    @Override
                    public void onSuccess() {
                    }

                    @Override
                    public void onError(Exception exception) {
                    }
                });
    }

    private List<Task> visibleTasksForCurrentUser(List<Task> tasks) {
        if (isCurrentUserOwner()) {
            return tasks;
        }
        List<Task> visibleTasks = new ArrayList<>();
        for (Task task : tasks) {
            if (currentUserId.equals(task.getAssigneeId())) {
                visibleTasks.add(task);
            }
        }
        return visibleTasks;
    }

    private boolean isCurrentUserOwner() {
        return currentProject != null && currentUserId.equals(currentProject.getOwnerId());
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

    private int percent(int value, int total) {
        return total == 0 ? 0 : Math.round(value * 100f / total);
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

    private void renderMissingProject() {
        currentProject = null;
        titleText.setText("Không tìm thấy dự án");
        descriptionText.setText("Dự án không tồn tại hoặc đã bị xóa.");
        dateText.setText("Từ -- đến --");
        ownerActions.setVisibility(View.GONE);
        reportButton.setVisibility(View.GONE);
        deleteButton.setVisibility(View.GONE);
        renderMembers(Collections.emptyList());
        renderTasks(Collections.emptyList(), Collections.emptyList());
    }
}
