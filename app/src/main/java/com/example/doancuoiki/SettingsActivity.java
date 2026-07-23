package com.example.doancuoiki;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.ComponentActivity;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.SwitchCompat;

public class SettingsActivity extends ComponentActivity {

    // Tên vùng lưu cài đặt trên thiết bị
    private static final String PREF_NAME = "APP_SETTINGS";

    // Key lưu trạng thái bật hoặc tắt thông báo
    private static final String KEY_NOTIFICATIONS =
            "notifications_enabled";

    private ImageView btnBack;
    private SwitchCompat switchNotifications;

    private SharedPreferences preferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        // Mở vùng lưu cài đặt
        preferences = getSharedPreferences(
                PREF_NAME,
                MODE_PRIVATE
        );

        bindViews();
        loadSettings();
        setupActions();
    }

    /**
     * Ánh xạ các thành phần từ XML sang Java.
     */
    private void bindViews() {
        btnBack = findViewById(R.id.btnBack);

        switchNotifications =
                findViewById(R.id.switchNotifications);
    }

    /**
     * Đọc trạng thái thông báo đã lưu.
     */
    private void loadSettings() {
        boolean notificationsEnabled =
                preferences.getBoolean(
                        KEY_NOTIFICATIONS,
                        true
                );

        switchNotifications.setChecked(
                notificationsEnabled
        );
    }

    /**
     * Cấu hình các sự kiện bấm.
     */
    private void setupActions() {

        // Nút quay lại
        btnBack.setOnClickListener(v -> finish());

        // Bật hoặc tắt thông báo
        switchNotifications.setOnCheckedChangeListener(
                (buttonView, isChecked) -> {

                    // Lưu trạng thái vào thiết bị
                    preferences.edit()
                            .putBoolean(
                                    KEY_NOTIFICATIONS,
                                    isChecked
                            )
                            .apply();

                    String message;

                    if (isChecked) {
                        message = "Đã bật thông báo";
                    } else {
                        message = "Đã tắt thông báo";
                    }

                    Toast.makeText(
                            SettingsActivity.this,
                            message,
                            Toast.LENGTH_SHORT
                    ).show();
                }
        );

        // Bấm vào mục Về TaskFlow
        findViewById(R.id.btnAppInfo)
                .setOnClickListener(v ->
                        showAppInfoDialog()
                );
    }

    /**
     * Hiển thị thông tin về ứng dụng.
     */
    private void showAppInfoDialog() {
        new AlertDialog.Builder(this)
                .setTitle("TaskFlow")
                .setMessage(
                        "Ứng dụng quản lý dự án và công việc nhóm.\n\n" +
                                "Phiên bản: 1.0\n" +
                                "Nhà phát triển: Nhóm sinh viên NTTU\n\n" +
                                "Các chức năng chính:\n" +
                                "• Quản lý dự án\n" +
                                "• Quản lý công việc\n" +
                                "• Theo dõi tiến độ\n" +
                                "• Thông báo công việc\n" +
                                "• Quản lý tài khoản\n" +
                                "• Gửi yêu cầu hỗ trợ"
                )
                .setPositiveButton("Đóng", null)
                .show();
    }
}