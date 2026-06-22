package com.example.doancuoiki;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.GridLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.doancuoiki.model.Task;
import com.example.doancuoiki.repository.TaskRepository;
import com.example.doancuoiki.utils.DateUtils;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class CalendarActivity extends Activity {
    private static final String GUEST_USER_ID = "guest";

    private final TaskRepository taskRepository = new TaskRepository();
    private final List<Task> allTasks = new ArrayList<>();

    private LinearLayout taskList;
    private TextView selectedDateText;
    private TextView calendarState;
    private TextView monthText;
    private GridLayout weekHeaderGrid;
    private GridLayout dayGrid;
    private Calendar visibleMonth = Calendar.getInstance();
    private String selectedDate;
    private String currentUserId = GUEST_USER_ID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calendar);
        NavigationUtils.setupBottomNav(this, NavigationUtils.CALENDAR);

        bindViews();
        resolveCurrentUser();
        setupCalendar();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (taskList != null) {
            loadTasks();
        }
    }

    private void bindViews() {
        taskList = findViewById(R.id.calendarTaskList);
        selectedDateText = findViewById(R.id.txtSelectedDate);
        calendarState = findViewById(R.id.txtCalendarState);
        monthText = findViewById(R.id.txtCalendarMonth);
        weekHeaderGrid = findViewById(R.id.calendarWeekHeader);
        dayGrid = findViewById(R.id.calendarDayGrid);
    }

    private void setupCalendar() {
        Calendar today = Calendar.getInstance();
        visibleMonth.set(Calendar.YEAR, today.get(Calendar.YEAR));
        visibleMonth.set(Calendar.MONTH, today.get(Calendar.MONTH));
        visibleMonth.set(Calendar.DAY_OF_MONTH, 1);

        selectedDate = DateUtils.fromCalendarDate(
                today.get(Calendar.YEAR),
                today.get(Calendar.MONTH),
                today.get(Calendar.DAY_OF_MONTH)
        );
        updateSelectedDateText();
        renderWeekHeader();
        renderCalendarDays();

        findViewById(R.id.btnPreviousMonth).setOnClickListener(v -> {
            visibleMonth.add(Calendar.MONTH, -1);
            renderCalendarDays();
        });

        findViewById(R.id.btnNextMonth).setOnClickListener(v -> {
            visibleMonth.add(Calendar.MONTH, 1);
            renderCalendarDays();
        });
    }

    private void resolveCurrentUser() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            currentUserId = user.getUid();
        }
    }

    private void loadTasks() {
        calendarState.setText("Đang tải công việc...");
        taskRepository.getTasksForUser(currentUserId, new TaskRepository.TaskListCallback() {
            @Override
            public void onSuccess(List<Task> tasks) {
                allTasks.clear();
                if (tasks.isEmpty()) {
                    calendarState.setText("Chưa có công việc nào trên lịch.");
                } else {
                    allTasks.addAll(tasks);
                    calendarState.setText(summaryText(tasks));
                }
                renderCalendarDays();
                renderTasksForSelectedDate();
            }

            @Override
            public void onError(Exception exception) {
                allTasks.clear();
                calendarState.setText("Không tải được công việc từ Firestore.");
                renderCalendarDays();
                renderTasksForSelectedDate();
            }
        });
    }

    private void renderWeekHeader() {
        weekHeaderGrid.removeAllViews();
        String[] days = {"CN", "T2", "T3", "T4", "T5", "T6", "T7"};
        for (String day : days) {
            TextView dayText = new TextView(this);
            dayText.setText(day);
            dayText.setGravity(Gravity.CENTER);
            dayText.setTextColor(Color.rgb(125, 132, 150));
            dayText.setTextSize(12);
            weekHeaderGrid.addView(dayText, gridParams(1, 32));
        }
    }

    private void renderCalendarDays() {
        dayGrid.removeAllViews();
        monthText.setText(String.format(
                Locale.getDefault(),
                "Tháng %d, %d",
                visibleMonth.get(Calendar.MONTH) + 1,
                visibleMonth.get(Calendar.YEAR)
        ));

        Calendar cursor = (Calendar) visibleMonth.clone();
        int firstDayOfWeek = cursor.get(Calendar.DAY_OF_WEEK) - 1;
        cursor.add(Calendar.DAY_OF_MONTH, -firstDayOfWeek);

        Set<String> taskDates = dueDateSet();
        for (int i = 0; i < 42; i++) {
            Calendar cellDate = (Calendar) cursor.clone();
            dayGrid.addView(dayCell(cellDate, taskDates.contains(dateKey(cellDate))), gridParams(1, 42));
            cursor.add(Calendar.DAY_OF_MONTH, 1);
        }
    }

    private View dayCell(Calendar cellDate, boolean hasTask) {
        LinearLayout cell = new LinearLayout(this);
        cell.setOrientation(LinearLayout.VERTICAL);
        cell.setGravity(Gravity.CENTER);
        cell.setPadding(0, dp(2), 0, dp(2));

        boolean isCurrentMonth = cellDate.get(Calendar.MONTH) == visibleMonth.get(Calendar.MONTH);
        boolean isSelected = DateUtils.isSameDate(
                DateUtils.fromCalendarDate(
                        cellDate.get(Calendar.YEAR),
                        cellDate.get(Calendar.MONTH),
                        cellDate.get(Calendar.DAY_OF_MONTH)
                ),
                selectedDate
        );
        boolean isToday = isSameCalendarDate(cellDate, Calendar.getInstance());

        TextView dayNumber = new TextView(this);
        dayNumber.setText(String.valueOf(cellDate.get(Calendar.DAY_OF_MONTH)));
        dayNumber.setGravity(Gravity.CENTER);
        dayNumber.setTextSize(13);
        dayNumber.setTextColor(isCurrentMonth ? Color.rgb(34, 38, 50) : Color.rgb(190, 195, 205));
        if (isSelected) {
            dayNumber.setTextColor(Color.WHITE);
            dayNumber.setBackgroundResource(R.drawable.bg_calendar_selected_day);
        } else if (isToday) {
            dayNumber.setBackgroundResource(R.drawable.bg_calendar_today);
            dayNumber.setTextColor(Color.rgb(34, 197, 94));
        }
        cell.addView(dayNumber, new LinearLayout.LayoutParams(dp(32), dp(32)));

        View dot = new View(this);
        LinearLayout.LayoutParams dotParams = new LinearLayout.LayoutParams(dp(6), dp(6));
        dotParams.topMargin = dp(2);
        if (hasTask) {
            dot.setBackgroundResource(R.drawable.bg_calendar_dot);
        }
        cell.addView(dot, dotParams);

        cell.setOnClickListener(v -> {
            selectedDate = DateUtils.fromCalendarDate(
                    cellDate.get(Calendar.YEAR),
                    cellDate.get(Calendar.MONTH),
                    cellDate.get(Calendar.DAY_OF_MONTH)
            );
            if (cellDate.get(Calendar.MONTH) != visibleMonth.get(Calendar.MONTH)
                    || cellDate.get(Calendar.YEAR) != visibleMonth.get(Calendar.YEAR)) {
                visibleMonth.set(Calendar.YEAR, cellDate.get(Calendar.YEAR));
                visibleMonth.set(Calendar.MONTH, cellDate.get(Calendar.MONTH));
                visibleMonth.set(Calendar.DAY_OF_MONTH, 1);
            }
            updateSelectedDateText();
            renderCalendarDays();
            renderTasksForSelectedDate();
        });

        return cell;
    }

    private Set<String> dueDateSet() {
        Set<String> dates = new HashSet<>();
        for (Task task : allTasks) {
            String normalized = DateUtils.normalize(task.getDueDate());
            if (!normalized.isEmpty()) {
                dates.add(normalized);
            }
        }
        return dates;
    }

    private String dateKey(Calendar calendar) {
        return DateUtils.fromCalendarDate(
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
        );
    }

    private boolean isSameCalendarDate(Calendar first, Calendar second) {
        return first.get(Calendar.YEAR) == second.get(Calendar.YEAR)
                && first.get(Calendar.MONTH) == second.get(Calendar.MONTH)
                && first.get(Calendar.DAY_OF_MONTH) == second.get(Calendar.DAY_OF_MONTH);
    }

    private void updateSelectedDateText() {
        selectedDateText.setText("Công việc ngày " + selectedDate);
    }

    private void renderTasksForSelectedDate() {
        taskList.removeAllViews();

        List<Task> tasksInDay = new ArrayList<>();
        for (Task task : allTasks) {
            if (DateUtils.isSameDate(task.getDueDate(), selectedDate)) {
                tasksInDay.add(task);
            }
        }

        if (tasksInDay.isEmpty()) {
            calendarState.setText("Không có công việc đến hạn trong ngày này.");
            return;
        }

        calendarState.setText("Có " + tasksInDay.size() + " công việc đến hạn.");
        for (Task task : tasksInDay) {
            View card = ViewFactory.taskCard(
                    this,
                    task.getTitle(),
                    valueOrDefault(task.getProjectName(), "Chưa chọn dự án") + " - Hạn: " + valueOrDefault(task.getDueDate(), "--"),
                    task.getStatus(),
                    badgeBackground(task.getStatus()),
                    badgeColor(task.getStatus())
            );
            card.setOnClickListener(v -> openTaskDetail(task));
            taskList.addView(card);
        }
    }

    private String summaryText(List<Task> tasks) {
        int dueSoon = 0;
        int done = 0;
        for (Task task : tasks) {
            if (Task.STATUS_DONE.equals(task.getStatus())) {
                done++;
            } else if (DateUtils.isDueSoon(task.getDueDate(), 3)) {
                dueSoon++;
            }
        }
        return "Có " + tasks.size() + " công việc, " + done + " đã hoàn thành, " + dueSoon + " sắp đến hạn.";
    }

    private void openTaskDetail(Task task) {
        Intent intent = new Intent(this, TaskDetailActivity.class);
        intent.putExtra(TaskDetailActivity.EXTRA_TASK_ID, task.getId());
        startActivity(intent);
    }

    private String valueOrDefault(String value, String defaultValue) {
        if (value == null || value.trim().isEmpty()) {
            return defaultValue;
        }
        return value.trim();
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

    private GridLayout.LayoutParams gridParams(int rowSpan, int heightDp) {
        GridLayout.LayoutParams params = new GridLayout.LayoutParams();
        params.width = 0;
        params.height = dp(heightDp);
        params.columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f);
        params.rowSpec = GridLayout.spec(GridLayout.UNDEFINED, rowSpan);
        return params;
    }

    private int dp(int value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }

}
