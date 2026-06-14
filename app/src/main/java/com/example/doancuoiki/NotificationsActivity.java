package com.example.doancuoiki;

import android.app.Activity;
import android.os.Bundle;
import android.widget.LinearLayout;

public class NotificationsActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notifications);

        LinearLayout list = findViewById(R.id.notificationList);
        list.addView(ViewFactory.notificationCard(this, "Bạn được giao công việc mới", "Thiết kế giao diện trang chủ", "2 phút trước"));
        list.addView(ViewFactory.notificationCard(this, "Công việc sắp đến hạn", "Xây dựng API sản phẩm sẽ đến hạn vào 10/06/2024", "1 giờ trước"));
        list.addView(ViewFactory.notificationCard(this, "Dự án đã được cập nhật", "Phần mềm quản lý đã được cập nhật tiến độ mới", "3 giờ trước"));
        list.addView(ViewFactory.notificationCard(this, "Cuộc họp sắp diễn ra", "Họp nhóm dự án vào 11:00 hôm nay", "5 giờ trước"));

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
    }
}
