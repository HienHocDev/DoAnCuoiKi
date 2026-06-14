package com.example.doancuoiki;

import android.content.Context;
import android.graphics.Color;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

public class HomeScreen {
    public View create(Context context, TaskFlowNavigator navigator) {
        ScrollView scroll = UiKit.screen(context);
        LinearLayout page = UiKit.page(context);
        scroll.addView(page);

        LinearLayout header = UiKit.horizontal(context);
        header.setGravity(Gravity.CENTER_VERTICAL);
        LinearLayout hello = UiKit.vertical(context);
        hello.addView(UiKit.text(context, "Xin chào,", 15, UiKit.MUTED, false));
        hello.addView(UiKit.text(context, "Nguyễn Văn A", 22, UiKit.TEXT, true));
        header.addView(hello, new LinearLayout.LayoutParams(0, -2, 1));
        TextView bell = UiKit.text(context, "Thông báo", 12, UiKit.PRIMARY, true);
        bell.setGravity(Gravity.CENTER);
        bell.setPadding(UiKit.dp(context, 12), 0, UiKit.dp(context, 12), 0);
        bell.setBackground(UiKit.round(context, Color.WHITE, 18));
        bell.setOnClickListener(v -> navigator.showNotifications());
        header.addView(bell, UiKit.lp(context, -2, 36));
        page.addView(header);

        LinearLayout summary = UiKit.vertical(context);
        summary.setPadding(UiKit.dp(context, 18), UiKit.dp(context, 18), UiKit.dp(context, 18), UiKit.dp(context, 18));
        summary.setBackground(UiKit.gradient(context, UiKit.PRIMARY, Color.rgb(124, 93, 250)));
        summary.addView(UiKit.text(context, "Tổng quan dự án", 16, Color.WHITE, true));

        LinearLayout numbers = UiKit.horizontal(context);
        numbers.setGravity(Gravity.CENTER);
        numbers.addView(stat(context, "12", "Dự án"));
        numbers.addView(stat(context, "36", "Công việc"));
        numbers.addView(stat(context, "24", "Hoàn thành"));
        summary.addView(numbers, UiKit.top(context, -1, -2, 12));
        page.addView(summary, UiKit.top(context, -1, -2, 20));

        UiKit.addSectionTitle(context, page, "Dự án của bạn", "Xem tất cả", v -> navigator.showProjects());
        for (int i = 0; i < SampleData.PROJECT_NAMES.length; i++) {
            int index = i;
            page.addView(UiKit.projectRow(
                    context,
                    SampleData.PROJECT_NAMES[index],
                    SampleData.PROJECT_TASKS[index] + " công việc - " + SampleData.PROJECT_PROGRESS[index] + "%",
                    SampleData.PROJECT_PROGRESS[index],
                    v -> navigator.showProjectDetail()
            ));
        }

        Button add = UiKit.primaryButton(context, "+  Thêm dự án");
        add.setOnClickListener(v -> navigator.showAddProject());
        page.addView(add, UiKit.top(context, -1, 52, 16));
        return scroll;
    }

    private TextView stat(Context context, String number, String label) {
        TextView view = UiKit.text(context, number + "\n" + label, 14, Color.WHITE, true);
        view.setGravity(Gravity.CENTER);
        view.setLayoutParams(new LinearLayout.LayoutParams(0, UiKit.dp(context, 70), 1));
        return view;
    }
}
