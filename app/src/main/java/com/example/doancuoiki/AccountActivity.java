package com.example.doancuoiki;

import android.app.Activity;
import android.app.AlertDialog;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.doancuoiki.model.User;
import com.example.doancuoiki.repository.AuthRepository;
import com.example.doancuoiki.repository.UserRepository;
import com.google.firebase.auth.FirebaseUser;

public class AccountActivity extends Activity {
    private final AuthRepository authRepository = new AuthRepository();
    private final UserRepository userRepository = new UserRepository();

    private ImageView avatarImage; // Đã chuẩn hóa tên biến thành avatarImage cho đúng chuẩn chuyên nghiệp
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
        avatarImage = findViewById(R.id.txtAvatar);
        profileNameText = findViewById(R.id.txtProfileName);
        profileRoleText = findViewById(R.id.txtProfileRole);
    }

    private void setupActions() {
        avatarImage.setOnClickListener(v -> showEditProfileDialog());
        findViewById(R.id.btnProfileInfo).setOnClickListener(v -> showEditProfileDialog());

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
        String name = valueOrDefault(user.getName(), "Người dùng");
        profileNameText.setText(name);
        profileRoleText.setText(valueOrDefault(user.getRole(), "Thành viên") + " - " + valueOrDefault(user.getEmail(), ""));

        String avatarUrl = user.getAvatarUrl();
        if (avatarUrl != null && !avatarUrl.trim().isEmpty()) {
            loadAvatarFromUrl(avatarUrl, avatarImage);
        } else {
            avatarImage.setImageResource(android.R.drawable.sym_def_app_icon);
            avatarImage.setBackgroundResource(R.drawable.bg_card);
        }
    }

    private void showEditProfileDialog() {
        if (firebaseUser == null) return;

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(40, 20, 40, 20);

        final EditText inputName = new EditText(this);
        inputName.setHint("Nhập tên mới");
        if (currentUser != null) {
            inputName.setText(currentUser.getName());
        }
        layout.addView(inputName);

        final EditText inputAvatar = new EditText(this);
        inputAvatar.setHint("Dán Link ảnh mạng (URL)");
        if (currentUser != null && currentUser.getAvatarUrl() != null) {
            inputAvatar.setText(currentUser.getAvatarUrl());
        }
        layout.addView(inputAvatar);

        new AlertDialog.Builder(this)
                .setTitle("Cập nhật thông tin")
                .setView(layout)
                .setPositiveButton("Lưu", (dialog, which) -> {
                    String name = inputName.getText().toString().trim();
                    String avatarUrl = inputAvatar.getText().toString().trim();

                    if (name.isEmpty()) {
                        NavigationUtils.showMessage(this, "Tên không được để trống");
                        return;
                    }

                    userRepository.updateName(firebaseUser.getUid(), name, new UserRepository.SimpleCallback() {
                        @Override
                        public void onSuccess() {
                            userRepository.updateAvatar(firebaseUser.getUid(), avatarUrl, new UserRepository.SimpleCallback() {
                                @Override
                                public void onSuccess() {
                                    NavigationUtils.showMessage(AccountActivity.this, "Đã cập nhật thông tin thành công!");
                                    loadProfile();
                                }

                                @Override
                                public void onError(Exception exception) {
                                    NavigationUtils.showMessage(AccountActivity.this, "Lỗi cập nhật ảnh: " + exception.getMessage());
                                }
                            });
                        }

                        @Override
                        public void onError(Exception exception) {
                            NavigationUtils.showMessage(AccountActivity.this, "Lỗi cập nhật tên: " + exception.getMessage());
                        }
                    });
                })
                .setNegativeButton("Hủy", null)
                .show();
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

    private String valueOrDefault(String value, String defaultValue) {
        if (value == null || value.trim().isEmpty()) {
            return defaultValue;
        }
        return value.trim();
    }

    private void loadAvatarFromUrl(String avatarUrl, ImageView imageView) {
        if (avatarUrl != null && !avatarUrl.trim().isEmpty() && imageView != null) {
            new Thread(() -> {
                try {
                    java.net.URL url = new java.net.URL(avatarUrl);
                    android.graphics.Bitmap bmp = android.graphics.BitmapFactory.decodeStream(url.openConnection().getInputStream());

                    if (bmp != null) {
                        int size = Math.min(bmp.getWidth(), bmp.getHeight());
                        android.graphics.Bitmap squareBmp = android.graphics.Bitmap.createBitmap(bmp, (bmp.getWidth() - size) / 2, (bmp.getHeight() - size) / 2, size, size);

                        android.graphics.Bitmap output = android.graphics.Bitmap.createBitmap(size, size, android.graphics.Bitmap.Config.ARGB_8888);
                        android.graphics.Canvas canvas = new android.graphics.Canvas(output);

                        final android.graphics.Paint paint = new android.graphics.Paint();
                        final android.graphics.Rect rect = new android.graphics.Rect(0, 0, size, size);

                        paint.setAntiAlias(true);
                        canvas.drawARGB(0, 0, 0, 0);
                        canvas.drawCircle(size / 2f, size / 2f, size / 2f, paint);
                        paint.setXfermode(new android.graphics.PorterDuffXfermode(android.graphics.PorterDuff.Mode.SRC_IN));
                        canvas.drawBitmap(squareBmp, rect, rect, paint);

                        runOnUiThread(() -> {
                            imageView.setImageBitmap(output);
                            imageView.setBackgroundResource(R.drawable.bg_card);
                        });
                    }
                } catch (Exception e) {
                    runOnUiThread(() -> imageView.setImageResource(android.R.drawable.sym_def_app_icon));
                }
            }).start();
        }
    }
}