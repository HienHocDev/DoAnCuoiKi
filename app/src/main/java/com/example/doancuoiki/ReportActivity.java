package com.example.doancuoiki;

import android.app.Activity;
import android.os.Bundle;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.doancuoiki.model.Task;
import com.example.doancuoiki.repository.TaskRepository;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ReportActivity extends Activity {
    private static final String GUEST_USER_ID = "guest";

    private final TaskRepository taskRepository = new TaskRepository();

    private TextView reportState;
    private TextView overallProgress;
    private TextView doneCount;
    private TextView doingCount;
    private TextView todoCount;
    private LinearLayout projectList;
    private LinearLayout memberList;
    private LinearLayout priorityList;
    private String currentUserId = GUEST_USER_ID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_report);

        bindViews();
        resolveCurrentUser();
        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
        loadReport();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (projectList != null) {
            loadReport();
        }
    }

    private void bindViews() {
        reportState = findViewById(R.id.txtReportState);
        overallProgress = findViewById(R.id.txtOverallProgress);
        doneCount = findViewById(R.id.txtDoneCount);
        doingCount = findViewById(R.id.txtDoingCount);
        todoCount = findViewById(R.id.txtTodoCount);
        projectList = findViewById(R.id.reportProjectList);
        memberList = findViewById(R.id.memberPerformanceList);
        priorityList = findViewById(R.id.priorityChartList);
    }

    private void resolveCurrentUser() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            currentUserId = user.getUid();
        }
    }

    private void loadReport() {
        reportState.setText("Đang tải báo cáo...");
        taskRepository.getTasksForUser(currentUserId, new TaskRepository.TaskListCallback() {
            @Override
            public void onSuccess(List<Task> tasks) {
                if (tasks.isEmpty()) {
                    reportState.setText("Chưa có công việc để tạo báo cáo.");
                } else {
                    reportState.setText("Đã tải " + tasks.size() + " công việc từ Firestore.");
                }
                renderReport(tasks);
            }

            @Override
            public void onError(Exception exception) {
                reportState.setText("Không tải được báo cáo từ Firestore.");
                renderReport(java.util.Collections.emptyList());
            }
        });
    }

    private void renderReport(List<Task> tasks) {
        int total = tasks.size();
        int done = countByStatus(tasks, Task.STATUS_DONE);
        int doing = countByStatus(tasks, Task.STATUS_IN_PROGRESS);
        int todo = countByStatus(tasks, Task.STATUS_NOT_STARTED);
        int progress = percent(done, total);

        overallProgress.setText(progress + "%\nHoàn thành");
        doneCount.setText(done + "\nHoàn thành");
        doingCount.setText(doing + "\nĐang làm");
        todoCount.setText(todo + "\nChưa bắt đầu");

        renderProjectProgress(tasks);
        renderMemberPerformance(tasks);
        renderPriorityChart(tasks);

        if (tasks.isEmpty()) {
            projectList.addView(ViewFactory.notificationCard(this, "Chưa có dữ liệu", "Tạo công việc để xem tiến độ theo dự án.", ""));
            memberList.addView(ViewFactory.notificationCard(this, "Chưa có dữ liệu", "Phân công công việc để xem hiệu suất thành viên.", ""));
            priorityList.addView(ViewFactory.notificationCard(this, "Chưa có dữ liệu", "Thêm độ ưu tiên cho công việc để xem biểu đồ.", ""));
        }
    }

    private void renderProjectProgress(List<Task> tasks) {
        projectList.removeAllViews();

        Map<String, ProjectStats> statsMap = new HashMap<>();
        for (Task task : tasks) {
            String projectName = valueOrDefault(task.getProjectName(), "Chưa chọn dự án");
            ProjectStats stats = statsMap.get(projectName);
            if (stats == null) {
                stats = new ProjectStats();
                statsMap.put(projectName, stats);
            }

            stats.total++;
            if (Task.STATUS_DONE.equals(task.getStatus())) {
                stats.done++;
            }
        }

        for (Map.Entry<String, ProjectStats> entry : statsMap.entrySet()) {
            ProjectStats stats = entry.getValue();
            int progress = percent(stats.done, stats.total);
            projectList.addView(ViewFactory.reportProgressCard(
                    this,
                    entry.getKey(),
                    stats.done + "/" + stats.total + " công việc hoàn thành",
                    progress
            ));
        }
    }

    private void renderPriorityChart(List<Task> tasks) {
        priorityList.removeAllViews();

        Map<String, Integer> statsMap = new HashMap<>();
        statsMap.put("Cao", 0);
        statsMap.put("Trung bình", 0);
        statsMap.put("Thấp", 0);

        for (Task task : tasks) {
            String priority = valueOrDefault(task.getPriority(), "Trung bình");
            if (!statsMap.containsKey(priority)) {
                statsMap.put(priority, 0);
            }
            statsMap.put(priority, statsMap.get(priority) + 1);
        }

        for (Map.Entry<String, Integer> entry : statsMap.entrySet()) {
            if (tasks.isEmpty()) {
                continue;
            }
            int progress = percent(entry.getValue(), tasks.size());
            priorityList.addView(ViewFactory.reportProgressCard(
                    this,
                    entry.getKey(),
                    entry.getValue() + "/" + tasks.size() + " công việc",
                    progress
            ));
        }
    }

    private void renderMemberPerformance(List<Task> tasks) {
        memberList.removeAllViews();

        Map<String, ProjectStats> statsMap = new HashMap<>();
        for (Task task : tasks) {
            String memberName = valueOrDefault(task.getAssigneeName(), "Chưa phân công");
            ProjectStats stats = statsMap.get(memberName);
            if (stats == null) {
                stats = new ProjectStats();
                statsMap.put(memberName, stats);
            }

            stats.total++;
            if (Task.STATUS_DONE.equals(task.getStatus())) {
                stats.done++;
            }
        }

        for (Map.Entry<String, ProjectStats> entry : statsMap.entrySet()) {
            ProjectStats stats = entry.getValue();
            int progress = percent(stats.done, stats.total);
            memberList.addView(ViewFactory.reportProgressCard(
                    this,
                    entry.getKey(),
                    stats.done + "/" + stats.total + " công việc hoàn thành",
                    progress
            ));
        }
    }

    private int countByStatus(List<Task> tasks, String status) {
        int count = 0;
        for (Task task : tasks) {
            if (status.equals(task.getStatus())) {
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

    private String valueOrDefault(String value, String defaultValue) {
        if (value == null || value.trim().isEmpty()) {
            return defaultValue;
        }
        return value.trim();
    }

    private static class ProjectStats {
        int total;
        int done;
    }
}
