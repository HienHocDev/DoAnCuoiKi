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
    private View emptyStateContainer;
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

        android.view.View mainLayout = findViewById(R.id.main_layout);
        if (mainLayout != null) {
            androidx.core.view.ViewCompat.setOnApplyWindowInsetsListener(mainLayout, (v, windowInsets) -> {
                androidx.core.graphics.Insets insets = windowInsets.getInsets(androidx.core.view.WindowInsetsCompat.Type.systemBars());
                v.setPadding(v.getPaddingLeft(), insets.top, v.getPaddingRight(), 0);
                return windowInsets;
            });
        }
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
        emptyStateContainer = findViewById(R.id.emptyStateContainer);
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
                allTasks.addAll(tasks);
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
            weekHeaderGrid.addView(dayText, gridParams(32));
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
            dayGrid.addView(dayCell(cellDate, taskDates.contains(dateKey(cellDate))), gridParams(45));
            cursor.add(Calendar.DAY_OF_MONTH, 1);
        }
    }

    private View dayCell(Calendar cellDate, boolean hasTask) {
        LinearLayout cell = new LinearLayout(this);
        cell.setOrientation(LinearLayout.VERTICAL);
        cell.setGravity(Gravity.CENTER);
        cell.setPadding(0, dp(2), 0, dp(4));

        boolean isCurrentMonth = cellDate.get(Calendar.MONTH) == visibleMonth.get(Calendar.MONTH);
        boolean isSelected = DateUtils.isSameDate(dateKey(cellDate), selectedDate);
        boolean isToday = isSameCalendarDate(cellDate, Calendar.getInstance());

        TextView dayNumber = new TextView(this);
        dayNumber.setText(String.valueOf(cellDate.get(Calendar.DAY_OF_MONTH)));
        dayNumber.setGravity(Gravity.CENTER);
        dayNumber.setTextSize(13);
        dayNumber.setTextColor(isCurrentMonth ? Color.parseColor("#222632") : Color.parseColor("#CBD5E1"));
        if (!isCurrentMonth) dayNumber.setAlpha(0.5f);
        if (isSelected) {
            dayNumber.setTextColor(Color.WHITE);
            android.graphics.drawable.GradientDrawable gd = new android.graphics.drawable.GradientDrawable();
            gd.setShape(android.graphics.drawable.GradientDrawable.OVAL);
            gd.setColor(Color.parseColor("#15B759"));
            dayNumber.setBackground(gd);
        } else if (isToday) {
            dayNumber.setBackgroundResource(R.drawable.bg_calendar_today);
            dayNumber.setTextColor(Color.rgb(34, 197, 94));
        }
        cell.addView(dayNumber, new LinearLayout.LayoutParams(dp(30), dp(30)));

        View dot = new View(this);
        LinearLayout.LayoutParams dotParams = new LinearLayout.LayoutParams(dp(5), dp(5));
        dotParams.topMargin = dp(4);
        if (hasTask && isCurrentMonth) {
            android.graphics.drawable.GradientDrawable dotGd = new android.graphics.drawable.GradientDrawable();
            dotGd.setShape(android.graphics.drawable.GradientDrawable.OVAL);
            dotGd.setColor(Color.parseColor("#F59E0B"));
            dot.setBackground(dotGd);
        }
        cell.addView(dot, dotParams);

        cell.setOnClickListener(v -> {
            selectedDate = dateKey(cellDate);
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
        calendarState.setVisibility(View.GONE);

        List<Task> tasksInDay = new ArrayList<>();
        for (Task task : allTasks) {
            if (DateUtils.isSameDate(task.getDueDate(), selectedDate)) {
                tasksInDay.add(task);
            }
        }

        if (tasksInDay.isEmpty()) {
            taskList.setVisibility(View.GONE);
            if (emptyStateContainer != null) emptyStateContainer.setVisibility(View.VISIBLE);
            return;
        }

        if (emptyStateContainer != null) emptyStateContainer.setVisibility(View.GONE);
        taskList.setVisibility(View.VISIBLE);
        
        for (Task task : tasksInDay) {
            View card = ViewFactory.taskCard(
                    this,
                    task.getTitle(),
                    valueOrDefault(task.getProjectName(), "Chưa chọn dự án")
                            + " - Hạn: " + valueOrDefault(task.getDueDate(), "--"),
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

    private GridLayout.LayoutParams gridParams(int heightDp) {
        GridLayout.LayoutParams params = new GridLayout.LayoutParams();
        params.width = 0;
        params.height = dp(heightDp);
        params.columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f);
        params.rowSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f);
        params.setGravity(Gravity.FILL);
        return params;
    }

    private int dp(int value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }
}
