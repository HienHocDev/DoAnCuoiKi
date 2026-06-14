package com.example.doancuoiki;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.widget.LinearLayout;

public class TasksActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tasks);
        NavigationUtils.setupBottomNav(this, NavigationUtils.TASKS);

        LinearLayout taskList = findViewById(R.id.taskList);
        taskList.addView(ViewFactory.taskCard(this, "Thiết kế giao diện trang chủ", "Website bán hàng - 30/05/2024", "Đang làm", R.drawable.bg_badge_yellow, Color.rgb(239, 173, 68)));
        taskList.addView(ViewFactory.taskCard(this, "Xây dựng API sản phẩm", "Website bán hàng - 10/08/2024", "Đang làm", R.drawable.bg_badge_yellow, Color.rgb(239, 173, 68)));
        taskList.addView(ViewFactory.taskCard(this, "Viết tài liệu hướng dẫn", "Phần mềm quản lý - 25/08/2024", "Hoàn thành", R.drawable.bg_badge_green, Color.rgb(33, 181, 127)));
        taskList.addView(ViewFactory.taskCard(this, "Nghiên cứu đối thủ", "Nghiên cứu thị trường - 16/05/2024", "Hoàn thành", R.drawable.bg_badge_green, Color.rgb(33, 181, 127)));

        findViewById(R.id.btnAddTask).setOnClickListener(v ->
                NavigationUtils.showDeveloping(this, "Tạo công việc"));
        findViewById(R.id.tabMine).setOnClickListener(v ->
                NavigationUtils.showDeveloping(this, "Lọc công việc của tôi"));
        findViewById(R.id.tabAssigned).setOnClickListener(v ->
                NavigationUtils.showDeveloping(this, "Lọc công việc đã giao"));
        findViewById(R.id.tabProject).setOnClickListener(v ->
                NavigationUtils.showDeveloping(this, "Lọc công việc theo dự án"));
    }
}
