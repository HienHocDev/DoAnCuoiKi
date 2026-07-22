package com.example.doancuoiki;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

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
    private static final int REQUEST_EDIT_PROJECT = 201;
    private static final String FILTER_ALL = "all";
    private static final String FILTER_PENDING = "pending";
    private static final String FILTER_DONE = "done";

    private final ProjectRepository projectRepository = new ProjectRepository();
    private final TaskRepository taskRepository = new TaskRepository();
    private final UserRepository userRepository = new UserRepository();

    private TextView titleText;
    private TextView descriptionText;
    private TextView dateText;
    private TextView ownerNoticeText;
    private TextView totalTaskText;
    private TextView doneTaskText;
    private TextView progressText;       // header progress label
    private TextView progressStatText;   // stat card progress %
    private ProgressBar headerProgressBar;
    private android.view.ViewGroup ownerNoticeContainer;
    private LinearLayout memberList;
    private LinearLayout taskList;
    private ProgressBar loadingSpinner;

    private View reportButton;
    private View deleteButton;
    private View addMemberButton;
    private View addTaskButton;
    private View editProjectButton;
    private View ownerBottomActions;  // container cho báo cáo + xóa

    // Filter tabs
    private TextView tabAllTasks;
    private TextView tabInProgressTasks;
    private TextView tabDoneTasks;
    private String currentTaskFilter = FILTER_ALL;

    private String projectId;
    private String currentUserId = "";
    private Project currentProject;
    private final List<User> displayedMembers = new ArrayList<>();
    private List<Task> allProjectTasks = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_project_detail);

        bindViews();
        updateFilterTabs(); // highlight "Tất cả" tab from the start
        View mainLayout = findViewById(android.R.id.content);
        if (mainLayout != null) {
            ViewCompat.setOnApplyWindowInsetsListener(mainLayout, (v, windowInsets) -> {
                Insets insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars());
                v.setPadding(v.getPaddingLeft(), insets.top, v.getPaddingRight(), insets.bottom);
                return windowInsets;
            });
        }
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        currentUserId = user == null ? "" : user.getUid();
        projectId = getIntent().getStringExtra(ProjectsActivity.EXTRA_PROJECT_ID);

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
        if (addMemberButton != null) addMemberButton.setOnClickListener(v -> showManageMembersDialog());
        if (addTaskButton != null) addTaskButton.setOnClickListener(v -> openAddTask());
        if (editProjectButton != null) editProjectButton.setOnClickListener(v -> openEditProject());
        if (reportButton != null) reportButton.setOnClickListener(v -> {
            Intent intent = new Intent(this, ReportActivity.class);
            intent.putExtra("projectId", projectId);
            startActivity(intent);
        });
        if (deleteButton != null) deleteButton.setOnClickListener(v -> confirmDeleteProject());

        // Filter tab listeners
        if (tabAllTasks != null) tabAllTasks.setOnClickListener(v -> setTaskFilter(FILTER_ALL));
        if (tabInProgressTasks != null) tabInProgressTasks.setOnClickListener(v -> setTaskFilter(FILTER_PENDING));
        if (tabDoneTasks != null) tabDoneTasks.setOnClickListener(v -> setTaskFilter(FILTER_DONE));

        loadProject();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (currentProject != null) {
            loadProjectTasks();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_EDIT_PROJECT && resultCode == RESULT_OK) {
            loadProject(); // Reload project data after edit
        }
    }

    private void bindViews() {
        titleText = findViewById(R.id.txtProjectTitle);
        descriptionText = findViewById(R.id.txtProjectDescription);
        dateText = findViewById(R.id.txtProjectDate);
        ownerNoticeText = findViewById(R.id.txtOwnerNotice);
        ownerNoticeContainer = findViewById(R.id.ownerNoticeContainer);
        totalTaskText = findViewById(R.id.txtProjectTaskCount);
        doneTaskText = findViewById(R.id.txtProjectDoneCount);
        progressText = findViewById(R.id.txtProjectProgress);       // header %
        progressStatText = findViewById(R.id.txtProjectProgressStat); // stat card %
        headerProgressBar = findViewById(R.id.progressBarDetail);
        memberList = findViewById(R.id.projectMemberList);
        taskList = findViewById(R.id.projectTaskList);
        loadingSpinner = findViewById(R.id.loadingSpinner);

        reportButton = findViewById(R.id.btnOpenReport);
        deleteButton = findViewById(R.id.btnDeleteProject);
        addMemberButton = findViewById(R.id.btnAddMember);
        addTaskButton = findViewById(R.id.btnAddProjectTask);
        editProjectButton = findViewById(R.id.btnEditProject);
        ownerBottomActions = findViewById(R.id.ownerBottomActions);

        tabAllTasks = findViewById(R.id.tabAllTasks);
        tabInProgressTasks = findViewById(R.id.tabInProgressTasks);
        tabDoneTasks = findViewById(R.id.tabDoneTasks);
    }

    private void setTaskFilter(String filter) {
        currentTaskFilter = filter;
        updateFilterTabs();
        renderFilteredTasks();
    }

    private void updateFilterTabs() {
        styleFilterTab(tabAllTasks, R.id.tabAllTasksUnderline, FILTER_ALL.equals(currentTaskFilter));
        styleFilterTab(tabInProgressTasks, R.id.tabInProgressTasksUnderline, FILTER_PENDING.equals(currentTaskFilter));
        styleFilterTab(tabDoneTasks, R.id.tabDoneTasksUnderline, FILTER_DONE.equals(currentTaskFilter));
    }

    private void styleFilterTab(TextView tab, int underlineId, boolean selected) {
        if (tab == null) return;
        View underline = findViewById(underlineId);
        if (selected) {
            tab.setTextColor(Color.parseColor("#15B759"));
            tab.setTypeface(null, Typeface.BOLD);
            if (underline != null) underline.setVisibility(View.VISIBLE);
        } else {
            tab.setTextColor(Color.parseColor("#7D8496"));
            tab.setTypeface(null, Typeface.NORMAL);
            if (underline != null) underline.setVisibility(View.INVISIBLE);
        }
        tab.setBackgroundResource(0);
    }

    private void renderFilteredTasks() {
        List<Task> filtered = new ArrayList<>();
        for (Task task : allProjectTasks) {
            if (FILTER_ALL.equals(currentTaskFilter)) {
                filtered.add(task);
            } else if (FILTER_DONE.equals(currentTaskFilter) && Task.STATUS_DONE.equals(task.getStatus())) {
                filtered.add(task);
            } else if (FILTER_PENDING.equals(currentTaskFilter) && !Task.STATUS_DONE.equals(task.getStatus())) {
                filtered.add(task);
            }
        }
        renderTaskCards(filtered);
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
        dateText.setText(valueOrDefault(currentProject.getStartDate(), "--")
                + " – " + valueOrDefault(currentProject.getEndDate(), "--"));

        boolean isOwner = isCurrentUserOwner();

        // Nút chỉnh sửa (góc phải trên)
        if (editProjectButton != null)
            editProjectButton.setVisibility(isOwner ? View.VISIBLE : View.GONE);

        // Bottom fixed bar: Thành viên + Thêm công việc (chỉ chủ dự án)
        View ownerActionsBar = findViewById(R.id.ownerActions);
        if (ownerActionsBar != null)
            ownerActionsBar.setVisibility(isOwner ? View.VISIBLE : View.GONE);

        // Container nút Báo cáo + Xóa (chỉ chủ dự án, trong scroll)
        if (ownerBottomActions != null)
            ownerBottomActions.setVisibility(isOwner ? View.VISIBLE : View.GONE);

        // Banner thông báo vai trò
        if (ownerNoticeContainer != null)
            ownerNoticeContainer.setVisibility(View.VISIBLE);
        if (ownerNoticeText != null) {
            ownerNoticeText.setText(isOwner
                    ? "Bạn là chủ dự án. Nhấn ✏️ để chỉnh sửa."
                    : "Bạn là thành viên. Xem và cập nhật công việc của mình.");
        }
    }

    private void openEditProject() {
        if (!isCurrentUserOwner() || currentProject == null) return;
        Intent intent = new Intent(this, EditProjectActivity.class);
        intent.putExtra(EditProjectActivity.EXTRA_PROJECT_ID, projectId);
        intent.putExtra(EditProjectActivity.EXTRA_PROJECT_NAME, currentProject.getName());
        intent.putExtra(EditProjectActivity.EXTRA_PROJECT_DESC, currentProject.getDescription());
        intent.putExtra(EditProjectActivity.EXTRA_PROJECT_START, currentProject.getStartDate());
        intent.putExtra(EditProjectActivity.EXTRA_PROJECT_END, currentProject.getEndDate());
        intent.putExtra(EditProjectActivity.EXTRA_OWNER_ID, currentProject.getOwnerId());
        startActivityForResult(intent, REQUEST_EDIT_PROJECT);
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

        boolean isOwner = isCurrentUserOwner();
        // Avatar stack
        View stack = ViewFactory.avatarStack(this, names, isOwner, v -> showManageMembersDialog());
        memberList.addView(stack, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));

        if (!users.isEmpty()) {
            TextView label = new TextView(this);
            label.setText(" " + users.size() + " thành viên");
            label.setTextColor(Color.parseColor("#7D8496"));
            label.setTextSize(11);
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT);
            lp.gravity = android.view.Gravity.CENTER_VERTICAL;
            memberList.addView(label, lp);
        }
    }

    /**
     * A3 — Quản lý thành viên: thêm hoặc xóa thành viên
     */
    private void showManageMembersDialog() {
        if (!isCurrentUserOwner()) {
            // Non-owner: show read-only member list
            showMemberListReadOnly();
            return;
        }

        String[] options = {"Thêm thành viên", "Xóa thành viên"};
        new AlertDialog.Builder(this)
                .setTitle("Quản lý thành viên")
                .setItems(options, (dialog, which) -> {
                    if (which == 0) showAddMemberDialog();
                    else showRemoveMemberDialog();
                })
                .show();
    }

    private void showMemberListReadOnly() {
        StringBuilder sb = new StringBuilder();
        for (User u : displayedMembers) {
            sb.append("• ").append(valueOrDefault(u.getName(), valueOrDefault(u.getEmail(), "Thành viên"))).append("\n");
        }
        new AlertDialog.Builder(this)
                .setTitle("Thành viên dự án")
                .setMessage(sb.length() == 0 ? "Chưa có thành viên" : sb.toString().trim())
                .setPositiveButton("Đóng", null)
                .show();
    }

    private void showAddMemberDialog() {
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

    private void showRemoveMemberDialog() {
        if (displayedMembers.isEmpty()) {
            NavigationUtils.showMessage(this, "Không có thành viên để xóa");
            return;
        }

        List<User> removableMembers = new ArrayList<>();
        for (User u : displayedMembers) {
            if (!u.getId().equals(currentProject.getOwnerId())) {
                removableMembers.add(u);
            }
        }

        if (removableMembers.isEmpty()) {
            NavigationUtils.showMessage(this, "Không có thành viên nào có thể xóa (chủ dự án không thể xóa)");
            return;
        }

        String[] memberNames = new String[removableMembers.size()];
        for (int i = 0; i < removableMembers.size(); i++) {
            User u = removableMembers.get(i);
            memberNames[i] = valueOrDefault(u.getName(), valueOrDefault(u.getEmail(), "Thành viên"));
        }

        new AlertDialog.Builder(this)
                .setTitle("Chọn thành viên để xóa")
                .setItems(memberNames, (dialog, which) -> {
                    User toRemove = removableMembers.get(which);
                    confirmRemoveMember(toRemove);
                })
                .show();
    }

    private void confirmRemoveMember(User user) {
        String name = valueOrDefault(user.getName(), valueOrDefault(user.getEmail(), "thành viên này"));
        new AlertDialog.Builder(this)
                .setTitle("Xóa thành viên")
                .setMessage("Bạn có chắc muốn xóa " + name + " khỏi dự án?")
                .setNegativeButton("Hủy", null)
                .setPositiveButton("Xóa", (dialog, which) -> removeMember(user))
                .show();
    }

    private void removeMember(User user) {
        projectRepository.removeMember(projectId, user.getId(),
                new ProjectRepository.SimpleCallback() {
                    @Override
                    public void onSuccess() {
                        if (currentProject.getMembers() != null) {
                            currentProject.getMembers().remove(user.getId());
                        }
                        displayedMembers.remove(user);
                        renderMembers(new ArrayList<>(displayedMembers));
                        NavigationUtils.showMessage(ProjectDetailActivity.this, "Đã xóa thành viên");
                    }

                    @Override
                    public void onError(Exception exception) {
                        NavigationUtils.showMessage(ProjectDetailActivity.this, "Không xóa được thành viên");
                    }
                });
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
            if (userId.equals(member.getId())) return true;
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
        if (loadingSpinner != null) loadingSpinner.setVisibility(View.VISIBLE);
        taskList.removeAllViews();

        taskRepository.getTasksByProject(currentProject.getId(), new TaskRepository.TaskListCallback() {
            @Override
            public void onSuccess(List<Task> tasks) {
                if (loadingSpinner != null) loadingSpinner.setVisibility(View.GONE);
                allProjectTasks = tasks;
                updateStats(tasks);
                renderFilteredTasks();
                updateProjectProgressIfNeeded(tasks);
            }

            @Override
            public void onError(Exception exception) {
                if (loadingSpinner != null) loadingSpinner.setVisibility(View.GONE);
                allProjectTasks = Collections.emptyList();
                renderTaskCards(Collections.emptyList());
            }
        });
    }

    private void updateStats(List<Task> tasks) {
        int total = tasks.size();
        int done = countDone(tasks);
        int progress = percent(done, total);

        // Stat card numbers (plain numbers only for new layout)
        if (totalTaskText != null) totalTaskText.setText(String.valueOf(total));
        if (doneTaskText != null) doneTaskText.setText(String.valueOf(done));
        if (progressStatText != null) progressStatText.setText(progress + "%");

        // Header progress label + progress bar
        if (progressText != null) progressText.setText(progress + "%");
        if (headerProgressBar != null) headerProgressBar.setProgress(progress);
    }

    /**
     * A4 — All members can see all project tasks. Badge "Của bạn" shown for assignee's own tasks.
     */
    private void renderTaskCards(List<Task> visibleTasks) {
        taskList.removeAllViews();

        if (visibleTasks.isEmpty()) {
            String message = isCurrentUserOwner()
                    ? "Dự án chưa có công việc. Hãy thêm công việc đầu tiên cho nhóm."
                    : "Không có công việc nào phù hợp bộ lọc.";
            taskList.addView(ViewFactory.notificationCard(this, "Chưa có công việc", message, ""));
            return;
        }

        for (Task task : visibleTasks) {
            boolean isMyTask = currentUserId.equals(task.getAssigneeId());
            String subInfo = valueOrDefault(task.getDueDate(), "Chưa có hạn") + " - "
                    + valueOrDefault(task.getAssigneeName(), "Chưa phân công")
                    + (isMyTask ? " ✓ Của bạn" : "");

            View card = ViewFactory.taskCard(
                    this,
                    task.getTitle(),
                    subInfo,
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
        if (!isCurrentUserOwner()) return;
        new AlertDialog.Builder(this)
                .setTitle("Xóa dự án")
                .setMessage("Bạn có chắc muốn xóa dự án này không? Tất cả công việc trong dự án cũng sẽ bị xóa.")
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
        if (currentProject == null || currentProject.getId() == null || !isCurrentUserOwner()) return;
        int progress = percent(countDone(tasks), tasks.size());
        if (progress == currentProject.getProgress()) return;
        currentProject.setProgress(progress);
        projectRepository.updateProgress(currentProject.getId(), progress,
                new ProjectRepository.SimpleCallback() {
                    @Override public void onSuccess() {}
                    @Override public void onError(Exception exception) {}
                });
    }

    private boolean isCurrentUserOwner() {
        return currentProject != null && currentUserId.equals(currentProject.getOwnerId());
    }

    private int countDone(List<Task> tasks) {
        int count = 0;
        for (Task task : tasks) {
            if (Task.STATUS_DONE.equals(task.getStatus())) count++;
        }
        return count;
    }

    private int percent(int value, int total) {
        return total == 0 ? 0 : Math.round(value * 100f / total);
    }

    private int badgeBackground(String status) {
        if (Task.STATUS_DONE.equals(status)) return R.drawable.bg_badge_green;
        if (Task.STATUS_IN_PROGRESS.equals(status)) return R.drawable.bg_badge_yellow;
        return R.drawable.bg_badge_blue;
    }

    private int badgeColor(String status) {
        if (Task.STATUS_DONE.equals(status)) return Color.rgb(33, 181, 127);
        if (Task.STATUS_IN_PROGRESS.equals(status)) return Color.rgb(239, 173, 68);
        return Color.rgb(34, 197, 94);
    }

    private String valueOrDefault(String value, String defaultValue) {
        return value == null || value.trim().isEmpty() ? defaultValue : value.trim();
    }

    private void renderMissingProject() {
        currentProject = null;
        titleText.setText("Không tìm thấy dự án");
        descriptionText.setText("Dự án không tồn tại hoặc đã bị xóa.");
        dateText.setText("📅 -- — --");

        if (editProjectButton != null) editProjectButton.setVisibility(View.GONE);
        if (reportButton != null) reportButton.setVisibility(View.GONE);
        if (addMemberButton != null) addMemberButton.setVisibility(View.GONE);
        if (addTaskButton != null) addTaskButton.setVisibility(View.GONE);
        if (ownerBottomActions != null) ownerBottomActions.setVisibility(View.GONE);
        renderMembers(Collections.emptyList());
        renderTaskCards(Collections.emptyList());
    }
}
