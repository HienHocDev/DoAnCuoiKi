package com.example.doancuoiki;

import android.app.Activity;
import android.os.Bundle;
import android.widget.CalendarView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class CalendarActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calendar);
        NavigationUtils.setupBottomNav(this, NavigationUtils.CALENDAR);

        LinearLayout taskList = findViewById(R.id.calendarTaskList);
        taskList.addView(ViewFactory.notificationCard(this, "Thiết kế giao diện trang chủ", "Website bán hàng", "08:00"));
        taskList.addView(ViewFactory.notificationCard(this, "Họp nhóm dự án", "Phần mềm quản lý", "11:00"));
        taskList.addView(ViewFactory.notificationCard(this, "Nghiên cứu đối thủ", "Nghiên cứu thị trường", "14:00"));

        TextView selectedDate = findViewById(R.id.txtSelectedDate);
        CalendarView calendarView = findViewById(R.id.calendarView);
        calendarView.setOnDateChangeListener((view, year, month, dayOfMonth) ->
                selectedDate.setText("Công việc ngày " + dayOfMonth + "/" + (month + 1) + "/" + year));
    }
}
