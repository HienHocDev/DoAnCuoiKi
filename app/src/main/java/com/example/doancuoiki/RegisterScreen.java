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
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.HashMap;
import java.util.Map;

public class RegisterScreen {
    public View create(Context context, TaskFlowNavigator navigator) {
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        ScrollView scrollView = UiKit.screen(context);
        LinearLayout page = UiKit.vertical(context);
        page.setPadding(UiKit.dp(context, 28), UiKit.dp(context, 46), UiKit.dp(context, 28), UiKit.dp(context, 28));
        page.setGravity(Gravity.CENTER_HORIZONTAL);
        scrollView.addView(page);

        // Tiêu đề màn hình
        TextView title = UiKit.text(context, "Đăng Ký Tài Khoản", 26, UiKit.PRIMARY, true);
        title.setGravity(Gravity.CENTER);
        page.addView(title, UiKit.top(context, -1, -2, 18));

        TextView subtitle = UiKit.text(context, "Tạo tài khoản mới để tham gia TaskFlow", 14, UiKit.TEXT, false);
        subtitle.setGravity(Gravity.CENTER);
        page.addView(subtitle, UiKit.top(context, -1, -2, 8));

        // Các ô nhập liệu
        EditText fullName = UiKit.input(context, "Họ tên của bạn");
        page.addView(fullName, UiKit.top(context, -1, 52, 34));

        EditText email = UiKit.input(context, "Email");
        page.addView(email, UiKit.top(context, -1, 52, 12));

        EditText password = UiKit.input(context, "Mật khẩu");
        password.setInputType(android.text.InputType.TYPE_CLASS_TEXT | android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD);
        page.addView(password, UiKit.top(context, -1, 52, 12));

        // NÚT ĐĂNG KÝ XỬ LÝ FIREBASE THẬT
        Button register = UiKit.primaryButton(context, "Đăng ký ngay");
        register.setOnClickListener(v -> {
            String nameStr = fullName.getText().toString().trim();
            String emailStr = email.getText().toString().trim();
            String passStr = password.getText().toString().trim();

            if (nameStr.isEmpty() || emailStr.isEmpty() || passStr.isEmpty()) {
                Toast.makeText(context, "Vui lòng nhập đầy đủ thông tin", Toast.LENGTH_SHORT).show();
                return;
            }

            if (passStr.length() < 6) {
                Toast.makeText(context, "Mật khẩu phải từ 6 ký tự trở lên", Toast.LENGTH_SHORT).show();
                return;
            }

            mAuth.createUserWithEmailAndPassword(emailStr, passStr)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            String uid = mAuth.getCurrentUser().getUid();

                            Map<String, Object> user = new HashMap<>();
                            user.put("id", uid);
                            user.put("name", nameStr);
                            user.put("email", emailStr);
                            user.put("role", "Thành viên");
                            user.put("avatarUrl", "");
                            user.put("createdAt", com.google.firebase.Timestamp.now());

                            db.collection("users").document(uid).set(user)
                                    .addOnSuccessListener(aVoid -> {
                                        Toast.makeText(context, "Đăng ký thành công!", Toast.LENGTH_SHORT).show();
                                        navigator.showLogin();
                                    })
                                    .addOnFailureListener(e -> {
                                        Toast.makeText(context, "Lưu dữ liệu thất bại: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                    });
                        } else {
                            Toast.makeText(context, "Lỗi đăng ký Auth: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                        }
                    });
        });
        page.addView(register, UiKit.top(context, -1, 52, 24));

        TextView backToLogin = UiKit.text(context, "Đã có tài khoản? Đăng nhập", 13, UiKit.MUTED, false);
        backToLogin.setGravity(Gravity.CENTER);
        backToLogin.setOnClickListener(v -> navigator.showLogin());
        page.addView(backToLogin, UiKit.top(context, -1, -2, 20));

        return scrollView;
    }
}