package com.example.doancuoiki;

import android.content.Context;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.firestore.FirebaseFirestore;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ProjectExtraScreens {
    public View addProject(Context context, TaskFlowNavigator navigator) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        ScrollView scroll = UiKit.screen(context);
        LinearLayout page = UiKit.page(context);
        scroll.addView(page);

        UiKit.addTopBar(context, page, "Thêm dự án", "", v -> navigator.showProjects(), null);

        TextView icon = UiKit.text(context, "+", 34, android.graphics.Color.WHITE, true);
        icon.setGravity(Gravity.CENTER);
        icon.setBackground(UiKit.round(context, UiKit.PRIMARY, 34));
        LinearLayout iconWrap = UiKit.vertical(context);
        iconWrap.setGravity(Gravity.CENTER);
        iconWrap.addView(icon, UiKit.lp(context, 68, 68));
        page.addView(iconWrap, UiKit.top(context, -1, 96, 20));

        // 1. Tên dự án
        page.addView(UiKit.text(context, "Tên dự án", 13, UiKit.TEXT, true), UiKit.top(context, -1, -2, 14));
        EditText inputName = UiKit.input(context, "Nhập tên dự án");
        page.addView(inputName, UiKit.top(context, -1, 52, 6));

        // 2. Mô tả
        page.addView(UiKit.text(context, "Mô tả", 13, UiKit.TEXT, true), UiKit.top(context, -1, -2, 14));
        EditText inputDesc = UiKit.input(context, "Nhập mô tả dự án");
        page.addView(inputDesc, UiKit.top(context, -1, 70, 6));

        // 3. Ngày bắt đầu (Đã nâng cấp lên chọn lịch)
        page.addView(UiKit.text(context, "Ngày bắt đầu", 13, UiKit.TEXT, true), UiKit.top(context, -1, -2, 14));
        EditText inputStart = UiKit.input(context, "Chọn ngày bắt đầu");
        inputStart.setFocusable(false); // Chặn hiện bàn phím chữ
        inputStart.setClickable(true);
        inputStart.setOnClickListener(v -> {
            java.util.Calendar calendar = java.util.Calendar.getInstance();
            int year = calendar.get(java.util.Calendar.YEAR);
            int month = calendar.get(java.util.Calendar.MONTH);
            int day = calendar.get(java.util.Calendar.DAY_OF_MONTH);

            android.app.DatePickerDialog datePickerDialog = new android.app.DatePickerDialog(context,
                    (view, selectedYear, selectedMonth, selectedDay) -> {
                        String date = String.format("%02d/%02d/%d", selectedDay, selectedMonth + 1, selectedYear);
                        inputStart.setText(date);
                    }, year, month, day);
            datePickerDialog.show();
        });
        page.addView(inputStart, UiKit.top(context, -1, 52, 6));

        // 4. Ngày kết thúc (Đã nâng cấp lên chọn lịch)
        page.addView(UiKit.text(context, "Ngày kết thúc", 13, UiKit.TEXT, true), UiKit.top(context, -1, -2, 14));
        EditText inputEnd = UiKit.input(context, "Chọn ngày kết thúc");
        inputEnd.setFocusable(false); // Chặn hiện bàn phím chữ
        inputEnd.setClickable(true);
        inputEnd.setOnClickListener(v -> {
            java.util.Calendar calendar = java.util.Calendar.getInstance();
            int year = calendar.get(java.util.Calendar.YEAR);
            int month = calendar.get(java.util.Calendar.MONTH);
            int day = calendar.get(java.util.Calendar.DAY_OF_MONTH);

            android.app.DatePickerDialog datePickerDialog = new android.app.DatePickerDialog(context,
                    (view, selectedYear, selectedMonth, selectedDay) -> {
                        String date = String.format("%02d/%02d/%d", selectedDay, selectedMonth + 1, selectedYear);
                        inputEnd.setText(date);
                    }, year, month, day);
            datePickerDialog.show();
        });
        page.addView(inputEnd, UiKit.top(context, -1, 52, 6));

        // 5. Thành viên
        page.addView(UiKit.text(context, "Thành viên", 13, UiKit.TEXT, true), UiKit.top(context, -1, -2, 14));
        EditText inputMembers = UiKit.input(context, "Chọn thành viên");
        page.addView(inputMembers, UiKit.top(context, -1, 52, 6));

        // NÚT LƯU DỰ ÁN LÊN FIREBASE THẬT
        Button create = UiKit.primaryButton(context, "Tạo dự án");
        create.setOnClickListener(v -> {
            String name = inputName.getText().toString().trim();
            String desc = inputDesc.getText().toString().trim();
            String startDate = inputStart.getText().toString().trim();
            String endDate = inputEnd.getText().toString().trim();

            if (name.isEmpty()) {
                Toast.makeText(context, "Vui lòng nhập tên dự án", Toast.LENGTH_SHORT).show();
                return;
            }

            String projectId = UUID.randomUUID().toString();

            Map<String, Object> project = new HashMap<>();
            project.put("id", projectId);
            project.put("name", name);
            project.put("description", desc);
            project.put("startDate", startDate);
            project.put("endDate", endDate);
            project.put("progress", 0);
            project.put("createdAt", com.google.firebase.Timestamp.now());

            db.collection("projects").document(projectId).set(project)
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(context, "Tạo dự án thành công!", Toast.LENGTH_SHORT).show();
                        navigator.showHome();
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(context, "Lỗi tạo dự án: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        });
        page.addView(create, UiKit.top(context, -1, 52, 28));
        return scroll;
    }

    public View projectDetail(Context context, TaskFlowNavigator navigator) {
        ScrollView scroll = UiKit.screen(context);
        LinearLayout page = UiKit.page(context);
        scroll.addView(page);

        UiKit.addTopBar(context, page, "Website bán hàng", "...", v -> navigator.showProjects(), v -> navigator.underDevelopment("Tùy chọn dự án"));
        page.addView(UiKit.tabs(context, "Tổng quan", "Công việc", "Thành viên", "Tệp tin"), UiKit.top(context, -1, -2, 14));

        LinearLayout stats = UiKit.horizontal(context);
        stats.addView(UiKit.metric(context, "10", "Tổng công việc"), new LinearLayout.LayoutParams(0, -1, 1));
        stats.addView(UiKit.metric(context, "7", "Hoàn thành"), new LinearLayout.LayoutParams(0, -1, 1));
        stats.addView(UiKit.metric(context, "70%", "Tiến độ"), new LinearLayout.LayoutParams(0, -1, 1));
        page.addView(stats, UiKit.top(context, -1, 100, 16));

        UiKit.addSectionTitle(context, page, "Danh sách công việc", "+", v -> navigator.underDevelopment("Thêm công việc"));
        UiKit.addTask(context, page, "Phân tích yêu cầu", "30/05/2024", "Nguyễn Văn A", "Hoàn thành", UiKit.SUCCESS, v -> navigator.underDevelopment("Chi tiết công việc"));
        UiKit.addTask(context, page, "Thiết kế giao diện", "30/05/2024", "Nguyễn Văn A", "Đang làm", UiKit.WARNING, v -> navigator.underDevelopment("Chi tiết công việc"));
        UiKit.addTask(context, page, "Xây dựng API", "10/06/2024", "Trần Thị B", "Đang làm", UiKit.WARNING, v -> navigator.underDevelopment("Chi tiết công việc"));
        UiKit.addTask(context, page, "Kiểm thử chức năng", "20/06/2024", "Lê Văn C", "Chưa bắt đầu", UiKit.PRIMARY, v -> navigator.underDevelopment("Chi tiết công việc"));

        Button report = UiKit.primaryButton(context, "Xem báo cáo tiến độ");
        report.setOnClickListener(v -> navigator.showReport());
        page.addView(report, UiKit.top(context, -1, 52, 18));
        return scroll;
    }

    public View report(Context context, TaskFlowNavigator navigator) {
        ScrollView scroll = UiKit.screen(context);
        LinearLayout page = UiKit.page(context);
        scroll.addView(page);

        UiKit.addTopBar(context, page, "Báo cáo tiến độ", "", v -> navigator.showProjectDetail(), null);
        TextView circle = UiKit.text(context, "75%", 36, UiKit.TEXT, true);
        circle.setGravity(Gravity.CENTER);
        circle.setBackground(UiKit.round(context, android.graphics.Color.rgb(238, 238, 255), 70));
        page.addView(circle, UiKit.top(context, -1, 150, 24));

        page.addView(UiKit.text(context, "Tiến độ theo dự án", 18, UiKit.TEXT, true), UiKit.top(context, -1, -2, 26));
        for (int i = 0; i < SampleData.PROJECT_NAMES.length; i++) {
            page.addView(UiKit.projectRow(context, SampleData.PROJECT_NAMES[i], SampleData.PROJECT_PROGRESS[i] + "%", SampleData.PROJECT_PROGRESS[i], v -> navigator.underDevelopment("Biểu đồ chi tiết")));
        }

        Button performance = UiKit.primaryButton(context, "Hiệu suất thành viên");
        performance.setOnClickListener(v -> navigator.showPerformance());
        page.addView(performance, UiKit.top(context, -1, 52, 18));
        return scroll;
    }

    public View performance(Context context, TaskFlowNavigator navigator) {
        ScrollView scroll = UiKit.screen(context);
        LinearLayout page = UiKit.page(context);
        scroll.addView(page);

        UiKit.addTopBar(context, page, "Hiệu suất thành viên", "", v -> navigator.showReport(), null);
        page.addView(UiKit.text(context, "Tháng 5, 2024", 16, UiKit.MUTED, false), UiKit.top(context, -1, -2, 16));

        String[] names = {"Nguyễn Văn A", "Trần Thị B", "Lê Văn C", "Phạm Thị D"};
        int[] done = {12, 8, 6, 4};
        for (int i = 0; i < names.length; i++) {
            page.addView(UiKit.projectRow(context, names[i], done[i] + " công việc hoàn thành", done[i] * 8, v -> navigator.underDevelopment("Chi tiết thành viên")));
        }
        return scroll;
    }

    public View notifications(Context context, TaskFlowNavigator navigator) {
        ScrollView scroll = UiKit.screen(context);
        LinearLayout page = UiKit.page(context);
        scroll.addView(page);

        UiKit.addTopBar(context, page, "Thông báo", "", v -> navigator.showHome(), null);
        addNotification(context, page, "Bạn được giao công việc mới", "Thiết kế giao diện trang chủ", "2 phút trước");
        addNotification(context, page, "Công việc sắp đến hạn", "Xây dựng API sản phẩm sẽ đến hạn vào 10/06/2024", "1 giờ trước");
        addNotification(context, page, "Dự án đã được cập nhật", "Phần mềm quản lý đã được cập nhật tiến độ mới", "3 giờ trước");
        addNotification(context, page, "Cuộc họp sắp diễn ra", "Họp nhóm dự án vào 11:00 hôm nay", "5 giờ trước");
        addNotification(context, page, "Công việc hoàn thành", "Nghiên cứu đối thủ đã hoàn thành", "1 ngày trước");
        return scroll;
    }

    private void addNotification(Context context, LinearLayout page, String title, String body, String time) {
        LinearLayout item = UiKit.vertical(context);
        item.setPadding(UiKit.dp(context, 16), UiKit.dp(context, 14), UiKit.dp(context, 16), UiKit.dp(context, 14));
        item.setBackground(UiKit.round(context, UiKit.CARD, 16));
        item.addView(UiKit.text(context, title, 15, UiKit.TEXT, true));
        item.addView(UiKit.text(context, body, 13, UiKit.MUTED, false), UiKit.top(context, -1, -2, 4));
        item.addView(UiKit.text(context, time, 12, android.graphics.Color.rgb(170, 176, 190), false), UiKit.top(context, -1, -2, 6));
        page.addView(item, UiKit.top(context, -1, -2, 12));
    }
}