package com.example.doancuoiki;

import android.content.Context;
import android.graphics.Color;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

public class AccountScreen {
    public View create(Context context, TaskFlowNavigator navigator) {
        ScrollView scroll = UiKit.screen(context);
        LinearLayout page = UiKit.page(context);
        scroll.addView(page);

        LinearLayout profile = UiKit.vertical(context);
        profile.setGravity(Gravity.CENTER);
        profile.setPadding(UiKit.dp(context, 20), UiKit.dp(context, 28), UiKit.dp(context, 20), UiKit.dp(context, 28));
        profile.setBackground(UiKit.gradient(context, Color.rgb(188, 115, 255), UiKit.PRIMARY));

        // Khai báo các TextView hiển thị thông tin profile
        TextView avatar = UiKit.text(context, "..", 28, UiKit.PRIMARY, true);
        avatar.setGravity(Gravity.CENTER);
        avatar.setBackground(UiKit.round(context, Color.WHITE, 44));
        profile.addView(avatar, UiKit.lp(context, 88, 88));

        TextView txtName = UiKit.text(context, "Đang tải...", 24, Color.WHITE, true);
        profile.addView(txtName, UiKit.top(context, -2, -2, 14));

        TextView txtRole = UiKit.text(context, "Thành viên", 15, Color.WHITE, false);
        profile.addView(txtRole);
        page.addView(profile, UiKit.lp(context, -1, 210));

        // --- TIẾN HÀNH LẤY DATA USER THẬT TỪ FIRESTORE ---
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            String uid = currentUser.getUid();
            FirebaseFirestore.getInstance().collection("users").document(uid).get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            String name = documentSnapshot.getString("name");
                            String role = documentSnapshot.getString("role");

                            if (name != null && !name.isEmpty()) {
                                txtName.setText(name);
                                // Thuật toán tự động cắt lấy chữ cái đầu làm Avatar (Ví dụ: "Dương" -> "D")
                                String[] words = name.split(" ");
                                if (words.length >= 2) {
                                    String initial = (words[words.length - 2].substring(0, 1) + words[words.length - 1].substring(0, 1)).toUpperCase();
                                    avatar.setText(initial);
                                } else {
                                    avatar.setText(name.substring(0, Math.min(2, name.length())).toUpperCase());
                                }
                            }
                            if (role != null && !role.isEmpty()) {
                                txtRole.setText(role);
                            }
                        }
                    });
        }

        addSetting(context, page, "Thông tin cá nhân", navigator);
        addSetting(context, page, "Đổi mật khẩu", navigator);
        addSetting(context, page, "Cài đặt thông báo", navigator);
        addSetting(context, page, "Ngôn ngữ: Tiếng Việt", navigator);
        addSetting(context, page, "Trợ giúp", navigator);

        // XỬ LÝ NÚT ĐĂNG XUẤT THẬT TỪ FIREBASE AUTH
        TextView logout = UiKit.setting(context, "Đăng xuất");
        logout.setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut(); // Lệnh kích đăng xuất thật trên hệ thống
            navigator.showLogin(); // Đăng xuất xong mới đá về màn hình đăng nhập
        });
        page.addView(logout, UiKit.top(context, -1, 52, 10));
        return scroll;
    }

    private void addSetting(Context context, LinearLayout page, String label, TaskFlowNavigator navigator) {
        TextView item = UiKit.setting(context, label);
        item.setOnClickListener(v -> navigator.underDevelopment(label));
        page.addView(item, UiKit.top(context, -1, 52, 10));
    }
}