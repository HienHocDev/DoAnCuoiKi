package com.example.doancuoiki;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.widget.LinearLayout;

public class ProjectDetailActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_project_detail);

        LinearLayout taskList = findViewById(R.id.projectTaskList);
        taskList.addView(ViewFactory.taskCard(this, "Phân tích yêu cầu", "30/05/2024 - Nguyễn Văn A", "Hoàn thành", R.drawable.bg_badge_green, Color.rgb(33, 181, 127)));
        taskList.addView(ViewFactory.taskCard(this, "Thiết kế giao diện", "30/05/2024 - Nguyễn Văn A", "Đang làm", R.drawable.bg_badge_yellow, Color.rgb(239, 173, 68)));
        taskList.addView(ViewFactory.taskCard(this, "Xây dựng API", "10/06/2024 - Trần Thị B", "Đang làm", R.drawable.bg_badge_yellow, Color.rgb(239, 173, 68)));
        taskList.addView(ViewFactory.taskCard(this, "Kiểm thử chức năng", "20/06/2024 - Lê Văn C", "Chưa bắt đầu", R.drawable.bg_badge_blue, Color.rgb(93, 95, 239)));

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
        findViewById(R.id.btnOpenReport).setOnClickListener(v ->
                NavigationUtils.open(this, ReportActivity.class));
    }
}
