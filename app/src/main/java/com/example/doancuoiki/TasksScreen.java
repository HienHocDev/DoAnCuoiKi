package com.example.doancuoiki;

import android.content.Context;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ScrollView;

public class TasksScreen {
    public View create(Context context, TaskFlowNavigator navigator) {
        ScrollView scroll = UiKit.screen(context);
        LinearLayout page = UiKit.page(context);
        scroll.addView(page);

        UiKit.addTopBar(context, page, "Công việc", "+", v -> navigator.showHome(), v -> navigator.underDevelopment("Tạo công việc"));
        page.addView(UiKit.searchBox(context, "Tìm kiếm công việc..."), UiKit.top(context, -1, 46, 18));
        page.addView(UiKit.tabs(context, "Tất cả", "Của tôi", "Đã giao", "Theo dự án"));

        UiKit.addTask(context, page, "Thiết kế giao diện trang chủ", "Website bán hàng", "30/05/2024", "Đang làm", UiKit.WARNING, v -> navigator.underDevelopment("Chi tiết công việc"));
        UiKit.addTask(context, page, "Xây dựng API sản phẩm", "Website bán hàng", "10/08/2024", "Đang làm", UiKit.WARNING, v -> navigator.underDevelopment("Chi tiết công việc"));
        UiKit.addTask(context, page, "Viết tài liệu hướng dẫn", "Phần mềm quản lý", "25/08/2024", "Hoàn thành", UiKit.SUCCESS, v -> navigator.underDevelopment("Chi tiết công việc"));
        UiKit.addTask(context, page, "Nghiên cứu đối thủ", "Nghiên cứu thị trường", "16/05/2024", "Hoàn thành", UiKit.SUCCESS, v -> navigator.underDevelopment("Chi tiết công việc"));
        return scroll;
    }
}
