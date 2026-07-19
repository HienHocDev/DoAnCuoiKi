package com.example.doancuoiki;

import android.app.Activity;
import android.os.Bundle;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.view.View;
import android.graphics.Color;

import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import java.util.ArrayList;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

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

    private PieChart pieChart;
    private TextView doneCount;
    private TextView doingCount;
    private TextView todoCount;
    private LinearLayout projectList;
    private LinearLayout memberList;
    private LinearLayout priorityList;
    private String currentUserId = GUEST_USER_ID;
    private String projectId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_report);

        View mainLayout = findViewById(android.R.id.content);
        if (mainLayout != null) {
            ViewCompat.setOnApplyWindowInsetsListener(mainLayout, (v, windowInsets) -> {
                Insets insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars());
                v.setPadding(v.getPaddingLeft(), insets.top, v.getPaddingRight(), insets.bottom);
                return windowInsets;
            });
        }

        projectId = getIntent().getStringExtra("projectId");

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
        pieChart = findViewById(R.id.pieChart);
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
        TaskRepository.TaskListCallback callback = new TaskRepository.TaskListCallback() {
            @Override
            public void onSuccess(List<Task> tasks) {
                renderReport(tasks);
            }

            @Override
            public void onError(Exception exception) {
                renderReport(java.util.Collections.emptyList());
            }
        };

        if (projectId != null && !projectId.isEmpty()) {
            taskRepository.getTasksByProject(projectId, callback);
        } else {
            taskRepository.getTasksForUser(currentUserId, callback);
        }
    }

    private void renderReport(List<Task> tasks) {
        int total = tasks.size();
        int done = countByStatus(tasks, Task.STATUS_DONE);
        int doing = countByStatus(tasks, Task.STATUS_IN_PROGRESS);
        int todo = countByStatus(tasks, Task.STATUS_NOT_STARTED);
        int progress = percent(done, total);

        setupPieChart(done, doing, todo, progress);
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

    private void setupPieChart(int done, int doing, int todo, int progress) {
        ArrayList<PieEntry> entries = new ArrayList<>();
        if (done > 0) entries.add(new PieEntry(done, "Hoàn thành"));
        if (doing > 0) entries.add(new PieEntry(doing, "Đang làm"));
        if (todo > 0) entries.add(new PieEntry(todo, "Chưa bắt đầu"));

        if (entries.isEmpty()) {
            pieChart.clear();
            return;
        }

        PieDataSet dataSet = new PieDataSet(entries, "");
        
        ArrayList<Integer> colors = new ArrayList<>();
        if (done > 0) colors.add(Color.parseColor("#15B759")); // Xanh lá
        if (doing > 0) colors.add(Color.parseColor("#FF9800")); // Cam
        if (todo > 0) colors.add(Color.parseColor("#7D8496")); // Xám
        dataSet.setColors(colors);

        dataSet.setValueTextColor(Color.WHITE);
        dataSet.setValueTextSize(14f);

        PieData data = new PieData(dataSet);
        
        pieChart.setData(data);
        pieChart.setUsePercentValues(false);
        pieChart.getDescription().setEnabled(false);
        pieChart.setDrawHoleEnabled(true);
        pieChart.setHoleColor(Color.TRANSPARENT);
        pieChart.setCenterText(progress + "%\nHoàn thành");
        pieChart.setCenterTextSize(24f);
        pieChart.setCenterTextColor(Color.parseColor("#222632"));
        
        Legend legend = pieChart.getLegend();
        legend.setVerticalAlignment(Legend.LegendVerticalAlignment.BOTTOM);
        legend.setHorizontalAlignment(Legend.LegendHorizontalAlignment.CENTER);
        legend.setOrientation(Legend.LegendOrientation.HORIZONTAL);
        legend.setDrawInside(false);
        
        pieChart.animateY(1000);
        pieChart.invalidate();
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

        Map<String, ProjectStats> statsMap = new HashMap<>();
        statsMap.put("Cao", new ProjectStats());
        statsMap.put("Trung bình", new ProjectStats());
        statsMap.put("Thấp", new ProjectStats());

        for (Task task : tasks) {
            String priority = valueOrDefault(task.getPriority(), "Trung bình");
            ProjectStats stats = statsMap.get(priority);
            if (stats == null) {
                stats = new ProjectStats();
                statsMap.put(priority, stats);
            }
            stats.total++;
            if (Task.STATUS_DONE.equals(task.getStatus())) {
                stats.done++;
            }
        }

        // We want to sort them: Cao, Trung bình, Thấp
        String[] order = {"Cao", "Trung bình", "Thấp"};
        for (String key : order) {
            ProjectStats stats = statsMap.get(key);
            if (stats != null) {
                int progress = stats.total > 0 ? percent(stats.done, stats.total) : 0;
                priorityList.addView(ViewFactory.reportProgressCard(
                        this,
                        key,
                        stats.done + "/" + stats.total + " công việc hoàn thành",
                        progress
                ));
            }
        }
        
        // Handle any custom priorities that might have been added
        for (Map.Entry<String, ProjectStats> entry : statsMap.entrySet()) {
            String key = entry.getKey();
            if (!key.equals("Cao") && !key.equals("Trung bình") && !key.equals("Thấp")) {
                ProjectStats stats = entry.getValue();
                int progress = stats.total > 0 ? percent(stats.done, stats.total) : 0;
                priorityList.addView(ViewFactory.reportProgressCard(
                        this,
                        key,
                        stats.done + "/" + stats.total + " công việc hoàn thành",
                        progress
                ));
            }
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
