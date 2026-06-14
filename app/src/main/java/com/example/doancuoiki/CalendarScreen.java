package com.example.doancuoiki;

import android.content.Context;
import android.graphics.Color;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

public class CalendarScreen {
    public View create(Context context, TaskFlowNavigator navigator) {
        ScrollView scroll = UiKit.screen(context);
        LinearLayout page = UiKit.page(context);
        scroll.addView(page);

        UiKit.addTopBar(context, page, "Lịch", "", v -> navigator.showHome(), null);
        TextView month = UiKit.text(context, "Tháng 5, 2024", 18, UiKit.TEXT, true);
        month.setGravity(Gravity.CENTER);
        page.addView(month, UiKit.top(context, -1, -2, 18));

        LinearLayout calendar = UiKit.vertical(context);
        calendar.setPadding(UiKit.dp(context, 12), UiKit.dp(context, 12), UiKit.dp(context, 12), UiKit.dp(context, 12));
        calendar.setBackground(UiKit.round(context, UiKit.CARD, 18));

        String[] days = {"T2", "T3", "T4", "T5", "T6", "T7", "CN"};
        LinearLayout names = UiKit.horizontal(context);
        for (String day : days) {
            names.addView(cell(context, day, false));
        }
        calendar.addView(names);

        for (int row = 0; row < 5; row++) {
            LinearLayout week = UiKit.horizontal(context);
            for (int col = 0; col < 7; col++) {
                int value = row * 7 + col + 1;
                week.addView(cell(context, String.valueOf(value), value == 16));
            }
            calendar.addView(week, UiKit.top(context, -1, 42, 6));
        }
        page.addView(calendar, UiKit.top(context, -1, -2, 18));

        page.addView(UiKit.text(context, "Công việc trong ngày", 18, UiKit.TEXT, true), UiKit.top(context, -1, -2, 24));
        addAgenda(context, page, "Thiết kế giao diện trang chủ", "Website bán hàng", "08:00");
        addAgenda(context, page, "Họp nhóm dự án", "Phần mềm quản lý", "11:00");
        addAgenda(context, page, "Nghiên cứu đối thủ", "Nghiên cứu thị trường", "14:00");
        return scroll;
    }

    private TextView cell(Context context, String value, boolean selected) {
        TextView cell = UiKit.text(context, value, 13, selected ? Color.WHITE : UiKit.TEXT, selected);
        cell.setGravity(Gravity.CENTER);
        if (selected) {
            cell.setBackground(UiKit.round(context, UiKit.PRIMARY, 22));
        }
        cell.setLayoutParams(new LinearLayout.LayoutParams(0, UiKit.dp(context, 38), 1));
        return cell;
    }

    private void addAgenda(Context context, LinearLayout page, String title, String project, String time) {
        LinearLayout row = UiKit.horizontal(context);
        row.setPadding(UiKit.dp(context, 14), UiKit.dp(context, 14), UiKit.dp(context, 14), UiKit.dp(context, 14));
        row.setBackground(UiKit.round(context, UiKit.CARD, 16));
        LinearLayout col = UiKit.vertical(context);
        col.addView(UiKit.text(context, title, 15, UiKit.TEXT, true));
        col.addView(UiKit.text(context, project, 12, UiKit.MUTED, false), UiKit.top(context, -1, -2, 4));
        row.addView(col, new LinearLayout.LayoutParams(0, -2, 1));
        row.addView(UiKit.text(context, time, 13, UiKit.MUTED, false));
        page.addView(row, UiKit.top(context, -1, -2, 12));
    }
}
