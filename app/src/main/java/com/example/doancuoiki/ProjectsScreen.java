package com.example.doancuoiki;

import android.content.Context;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ScrollView;

public class ProjectsScreen {
    public View create(Context context, TaskFlowNavigator navigator) {
        ScrollView scroll = UiKit.screen(context);
        LinearLayout page = UiKit.page(context);
        scroll.addView(page);

        UiKit.addTopBar(context, page, "Dự án", "+", v -> navigator.showHome(), v -> navigator.showAddProject());
        page.addView(UiKit.searchBox(context, "Tìm kiếm dự án..."), UiKit.top(context, -1, 46, 18));

        for (int i = 0; i < SampleData.PROJECT_NAMES.length; i++) {
            page.addView(projectCard(context, SampleData.PROJECT_NAMES[i], SampleData.PROJECT_TASKS[i], SampleData.PROJECT_PROGRESS[i], navigator));
        }
        return scroll;
    }

    private View projectCard(Context context, String name, int tasks, int progress, TaskFlowNavigator navigator) {
        LinearLayout card = UiKit.vertical(context);
        card.setPadding(UiKit.dp(context, 18), UiKit.dp(context, 16), UiKit.dp(context, 18), UiKit.dp(context, 16));
        card.setBackground(UiKit.round(context, UiKit.CARD, 18));
        card.setOnClickListener(v -> navigator.showProjectDetail());
        card.addView(UiKit.text(context, name, 16, UiKit.TEXT, true));
        card.addView(UiKit.text(context, tasks + " công việc - " + progress + "%", 13, UiKit.MUTED, false), UiKit.top(context, -1, -2, 6));
        card.addView(UiKit.progress(context, progress), UiKit.top(context, -1, 8, 10));
        card.addView(UiKit.text(context, "Thành viên: Nguyễn Văn A, Trần Thị B, Lê Văn C", 12, UiKit.MUTED, false), UiKit.top(context, -1, -2, 12));
        return UiKit.withTop(context, card, 14);
    }
}
