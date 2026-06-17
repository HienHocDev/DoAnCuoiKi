package com.example.doancuoiki;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.widget.CalendarView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.doancuoiki.model.Task;
import com.example.doancuoiki.repository.TaskRepository;
import com.example.doancuoiki.utils.DateUtils;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class CalendarActivity extends Activity {
    private final TaskRepository taskRepository = new TaskRepository();
    private final List<Task> allTasks = new ArrayList<>();

    private LinearLayout taskList;
    private TextView selectedDateText;
    private TextView calendarState;
    private String selectedDate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calendar);
        NavigationUtils.setupBottomNav(this, NavigationUtils.CALENDAR);

        bindViews();
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
    }

    private void setupCalendar() {
        Calendar today = Calendar.getInstance();
        selectedDate = DateUtils.fromCalendarDate(
                today.get(Calendar.YEAR),
                today.get(Calendar.MONTH),
                today.get(Calendar.DAY_OF_MONTH)
        );
        selectedDateText.setText("Công việc ngày " + selectedDate);

        CalendarView calendarView = findViewById(R.id.calendarView);
        calendarView.setOnDateChangeListener((view, year, month, dayOfMonth) -> {
            selectedDate = DateUtils.fromCalendarDate(year, month, dayOfMonth);
            selectedDateText.setText("Công việc ngày " + selectedDate);
            renderTasksForSelectedDate();
        });
    }

    private void loadTasks() {
        calendarState.setText("Đang tải công việc...");
        taskRepository.getAllTasks(new TaskRepository.TaskListCallback() {
            @Override
            public void onSuccess(List<Task> tasks) {
                allTasks.clear();
                if (tasks.isEmpty()) {
                    calendarState.setText("Chưa có công việc nào trên lịch.");
                } else {
                    allTasks.addAll(tasks);
                    calendarState.setText("Đã tải " + tasks.size() + " công việc từ Firestore.");
                }
                renderTasksForSelectedDate();
            }

            @Override
            public void onError(Exception exception) {
                allTasks.clear();
                calendarState.setText("Không tải được công việc từ Firestore.");
                renderTasksForSelectedDate();
            }
        });
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
            taskList.addView(ViewFactory.taskCard(
                    this,
                    task.getTitle(),
                    task.getProjectName() + " - Hạn: " + task.getDueDate(),
                    task.getStatus(),
                    badgeBackground(task.getStatus()),
                    badgeColor(task.getStatus())
            ));
        }
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
        return Color.rgb(93, 95, 239);
    }

}
