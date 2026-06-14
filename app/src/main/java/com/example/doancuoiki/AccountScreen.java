package com.example.doancuoiki;

import android.content.Context;
import android.graphics.Color;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

public class AccountScreen {
    public View create(Context context, TaskFlowNavigator navigator) {
        ScrollView scroll = UiKit.screen(context);
        LinearLayout page = UiKit.page(context);
        scroll.addView(page);

        LinearLayout profile = UiKit.vertical(context);
        profile.setGravity(Gravity.CENTER);
        profile.setPadding(UiKit.dp(context, 20), UiKit.dp(context, 28), UiKit.dp(context, 20), UiKit.dp(context, 28));
        profile.setBackground(UiKit.gradient(context, Color.rgb(188, 115, 255), UiKit.PRIMARY));

        TextView avatar = UiKit.text(context, "NA", 28, UiKit.PRIMARY, true);
        avatar.setGravity(Gravity.CENTER);
        avatar.setBackground(UiKit.round(context, Color.WHITE, 44));
        profile.addView(avatar, UiKit.lp(context, 88, 88));
        profile.addView(UiKit.text(context, "Nguyễn Văn A", 24, Color.WHITE, true), UiKit.top(context, -2, -2, 14));
        profile.addView(UiKit.text(context, "Trưởng nhóm", 15, Color.WHITE, false));
        page.addView(profile, UiKit.lp(context, -1, 210));

        addSetting(context, page, "Thông tin cá nhân", navigator);
        addSetting(context, page, "Đổi mật khẩu", navigator);
        addSetting(context, page, "Cài đặt thông báo", navigator);
        addSetting(context, page, "Ngôn ngữ: Tiếng Việt", navigator);
        addSetting(context, page, "Trợ giúp", navigator);

        TextView logout = UiKit.setting(context, "Đăng xuất");
        logout.setOnClickListener(v -> navigator.showLogin());
        page.addView(logout, UiKit.top(context, -1, 52, 10));
        return scroll;
    }

    private void addSetting(Context context, LinearLayout page, String label, TaskFlowNavigator navigator) {
        TextView item = UiKit.setting(context, label);
        item.setOnClickListener(v -> navigator.underDevelopment(label));
        page.addView(item, UiKit.top(context, -1, 52, 10));
    }
}
