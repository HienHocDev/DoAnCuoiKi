package com.example.doancuoiki;

import android.app.Activity;
import android.os.Bundle;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.doancuoiki.model.Task;
import com.example.doancuoiki.repository.TaskRepository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ReportActivity extends Activity {
    private final TaskRepository taskRepository = new TaskRepository();

    private TextView reportState;
    private TextView overallProgress;
    private TextView doneCount;
    private TextView doingCount;
    private TextView todoCount;
    private LinearLayout projectList;
    private LinearLayout memberList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_report);

        bindViews();
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
    }

    private void loadReport() {
        reportState.setText("Đang tải báo cáo...");
        taskRepository.getAllTasks(new TaskRepository.TaskListCallback() {
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

        if (tasks.isEmpty()) {
            projectList.addView(ViewFactory.notificationCard(this, "Chưa có dữ liệu", "Tạo công việc để xem tiến độ theo dự án.", ""));
            memberList.addView(ViewFactory.notificationCard(this, "Chưa có dữ liệu", "Phân công công việc để xem hiệu suất thành viên.", ""));
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
