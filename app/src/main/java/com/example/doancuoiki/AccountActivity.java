package com.example.doancuoiki;

import android.app.Activity;
import android.app.AlertDialog;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.TextView;

import com.example.doancuoiki.model.User;
import com.example.doancuoiki.repository.AuthRepository;
import com.example.doancuoiki.repository.UserRepository;
import com.google.firebase.auth.FirebaseUser;

public class AccountActivity extends Activity {
    private final AuthRepository authRepository = new AuthRepository();
    private final UserRepository userRepository = new UserRepository();

    private TextView avatarText;
    private TextView profileNameText;
    private TextView profileRoleText;

    private FirebaseUser firebaseUser;
    private User currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account);
        NavigationUtils.setupBottomNav(this, NavigationUtils.ACCOUNT);

        bindViews();
        setupActions();
        loadProfile();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadProfile();
    }

    private void bindViews() {
        avatarText = findViewById(R.id.txtAvatar);
        profileNameText = findViewById(R.id.txtProfileName);
        profileRoleText = findViewById(R.id.txtProfileRole);
    }

    private void setupActions() {
        findViewById(R.id.btnProfileInfo).setOnClickListener(v -> showEditNameDialog());
        findViewById(R.id.btnChangePassword).setOnClickListener(v -> sendPasswordReset());
        findViewById(R.id.btnNotificationSetting).setOnClickListener(v ->
                NavigationUtils.open(this, NotificationsActivity.class));
        findViewById(R.id.btnLanguage).setOnClickListener(v ->
                NavigationUtils.showMessage(this, "Ngôn ngữ hiện tại: Tiếng Việt"));
        findViewById(R.id.btnHelp).setOnClickListener(v ->
                NavigationUtils.showMessage(this, "Liên hệ nhóm phát triển TaskFlow"));
        findViewById(R.id.btnLogout).setOnClickListener(v -> logout());
    }

    private void loadProfile() {
        firebaseUser = authRepository.getCurrentUser();
        if (firebaseUser == null) {
            NavigationUtils.openAndFinish(this, LoginActivity.class);
            return;
        }

        profileNameText.setText("Đang tải...");
        profileRoleText.setText(firebaseUser.getEmail());

        userRepository.getUser(firebaseUser.getUid(), new UserRepository.UserCallback() {
            @Override
            public void onSuccess(User user) {
                currentUser = user;
                renderProfile(user);
            }

            @Override
            public void onError(Exception exception) {
                currentUser = new User(
                        firebaseUser.getUid(),
                        valueOrDefault(firebaseUser.getEmail(), "Người dùng"),
                        valueOrDefault(firebaseUser.getEmail(), ""),
                        "Thành viên",
                        "",
                        ""
                );
                renderProfile(currentUser);
            }
        });
    }

    private void renderProfile(User user) {
        profileNameText.setText(valueOrDefault(user.getName(), "Người dùng"));
        profileRoleText.setText(valueOrDefault(user.getRole(), "Thành viên") + " - " + valueOrDefault(user.getEmail(), ""));
        avatarText.setText(initials(user.getName()));
    }

    private void showEditNameDialog() {
        if (firebaseUser == null) {
            return;
        }

        EditText input = new EditText(this);
        input.setHint("Nhập tên mới");
        if (currentUser != null) {
            input.setText(currentUser.getName());
        }

        new AlertDialog.Builder(this)
                .setTitle("Cập nhật thông tin")
                .setView(input)
                .setPositiveButton("Lưu", (dialog, which) -> updateName(input.getText().toString().trim()))
                .setNegativeButton("Hủy", null)
                .show();
    }

    private void updateName(String name) {
        if (name.isEmpty()) {
            NavigationUtils.showMessage(this, "Tên không được để trống");
            return;
        }

        userRepository.updateName(firebaseUser.getUid(), name, new UserRepository.SimpleCallback() {
            @Override
            public void onSuccess() {
                NavigationUtils.showMessage(AccountActivity.this, "Đã cập nhật tên");
                loadProfile();
            }

            @Override
            public void onError(Exception exception) {
                NavigationUtils.showMessage(AccountActivity.this, "Cập nhật thất bại: " + exception.getMessage());
            }
        });
    }

    private void sendPasswordReset() {
        if (firebaseUser == null || firebaseUser.getEmail() == null) {
            NavigationUtils.showMessage(this, "Không tìm thấy email tài khoản");
            return;
        }

        authRepository.sendPasswordReset(firebaseUser.getEmail(), new AuthRepository.SimpleCallback() {
            @Override
            public void onSuccess() {
                NavigationUtils.showMessage(AccountActivity.this, "Đã gửi email đổi mật khẩu");
            }

            @Override
            public void onError(Exception exception) {
                NavigationUtils.showMessage(AccountActivity.this, "Không gửi được email: " + exception.getMessage());
            }
        });
    }

    private void logout() {
        authRepository.logout();
        NavigationUtils.openAndFinish(this, LoginActivity.class);
    }

    private String initials(String name) {
        String value = valueOrDefault(name, "User").trim();
        if (value.isEmpty()) {
            return "U";
        }

        String[] parts = value.split("\\s+");
        if (parts.length == 1) {
            return parts[0].substring(0, 1).toUpperCase();
        }

        return (parts[0].substring(0, 1) + parts[parts.length - 1].substring(0, 1)).toUpperCase();
    }

    private String valueOrDefault(String value, String defaultValue) {
        if (value == null || value.trim().isEmpty()) {
            return defaultValue;
        }
        return value.trim();
    }
}
