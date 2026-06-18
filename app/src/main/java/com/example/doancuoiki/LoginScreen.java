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
import com.google.firebase.auth.FirebaseAuth;

public class LoginScreen {
    public View create(Context context, TaskFlowNavigator navigator) {
        FirebaseAuth mAuth = FirebaseAuth.getInstance();

        ScrollView scrollView = UiKit.screen(context);
        LinearLayout page = UiKit.vertical(context);
        page.setPadding(UiKit.dp(context, 28), UiKit.dp(context, 46), UiKit.dp(context, 28), UiKit.dp(context, 28));
        page.setGravity(Gravity.CENTER_HORIZONTAL);
        scrollView.addView(page);

        TextView illustration = UiKit.text(context, "TASKFLOW", 28, UiKit.PRIMARY, true);
        illustration.setGravity(Gravity.CENTER);
        illustration.setBackground(UiKit.round(context, android.graphics.Color.rgb(238, 238, 255), 26));
        page.addView(illustration, UiKit.lp(context, -1, 130));

        TextView title = UiKit.text(context, "TaskFlow", 28, UiKit.PRIMARY, true);
        title.setGravity(Gravity.CENTER);
        page.addView(title, UiKit.top(context, -1, -2, 18));

        TextView subtitle = UiKit.text(context, "Quản lý dự án và công việc\nhóm hiệu quả", 15, UiKit.TEXT, false);
        subtitle.setGravity(Gravity.CENTER);
        page.addView(subtitle, UiKit.top(context, -1, -2, 8));

        EditText email = UiKit.input(context, "Email");
        page.addView(email, UiKit.top(context, -1, 52, 34));

        EditText password = UiKit.input(context, "Mật khẩu");
        // Ẩn mật khẩu thành dấu chấm bảo mật
        password.setInputType(android.text.InputType.TYPE_CLASS_TEXT | android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD);
        page.addView(password, UiKit.top(context, -1, 52, 12));

        // 1. CHỨC NĂNG QUÊN MẬT KHẨU THẬT
        TextView forgot = UiKit.text(context, "Quên mật khẩu?", 13, UiKit.PRIMARY_DARK, false);
        forgot.setGravity(Gravity.RIGHT);
        forgot.setOnClickListener(v -> {
            String emailStr = email.getText().toString().trim();
            if (emailStr.isEmpty()) {
                Toast.makeText(context, "Vui lòng điền Email vào ô phía trên để nhận link đặt lại mật khẩu!", Toast.LENGTH_SHORT).show();
            } else {
                mAuth.sendPasswordResetEmail(emailStr).addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(context, "Đã gửi email khôi phục mật khẩu. Hãy kiểm tra hộp thư!", Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(context, "Lỗi: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
        page.addView(forgot, UiKit.top(context, -1, -2, 8));

        // 2. CHỨC NĂNG ĐĂNG NHẬP THẬT BẰNG FIREBASE (ĐÃ SỬA LỖI ĐIỀU HƯỚNG)
        Button login = UiKit.primaryButton(context, "Đăng nhập");
        login.setOnClickListener(v -> {
            String emailStr = email.getText().toString().trim();
            String passStr = password.getText().toString().trim();

            if (emailStr.isEmpty() || passStr.isEmpty()) {
                Toast.makeText(context, "Vui lòng nhập đầy đủ email và mật khẩu", Toast.LENGTH_SHORT).show();
                return;
            }

            mAuth.signInWithEmailAndPassword(emailStr, passStr)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Toast.makeText(context, "Đăng nhập thành công!", Toast.LENGTH_SHORT).show();

                            // Đưa lệnh điều hướng vào MainLooper giao diện để đổi màn hình ngay lập tức
                            new android.os.Handler(android.os.Looper.getMainLooper()).postDelayed(() -> {
                                navigator.showHome();
                            }, 200);

                        } else {
                            Toast.makeText(context, "Sai tài khoản hoặc mật khẩu: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                        }
                    });
        });
        page.addView(login, UiKit.top(context, -1, 52, 14));

        TextView social = UiKit.text(context, "hoặc đăng nhập với", 13, UiKit.MUTED, false);
        social.setGravity(Gravity.CENTER);
        page.addView(social, UiKit.top(context, -1, -2, 18));

        LinearLayout socialRow = UiKit.horizontal(context);
        socialRow.setGravity(Gravity.CENTER);
        TextView google = UiKit.lightButton(context, "Google");
        google.setOnClickListener(v -> navigator.underDevelopment("Google Login"));
        TextView facebook = UiKit.lightButton(context, "Facebook");
        facebook.setOnClickListener(v -> navigator.underDevelopment("Facebook Login"));
        socialRow.addView(google);
        socialRow.addView(facebook);
        page.addView(socialRow, UiKit.top(context, -1, 48, 10));

        // 3. ĐIỀU HƯỚNG SANG MÀN HÌNH ĐĂNG KÝ
        TextView register = UiKit.text(context, "Chưa có tài khoản? Đăng ký ngay", 13, UiKit.MUTED, false);
        register.setGravity(Gravity.CENTER);
        register.setOnClickListener(v -> {
            navigator.showRegister();
        });
        page.addView(register, UiKit.top(context, -1, -2, 24));

        return scrollView;
    }
}