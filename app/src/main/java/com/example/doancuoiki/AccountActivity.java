package com.example.doancuoiki;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.doancuoiki.model.User;
import com.example.doancuoiki.repository.AuthRepository;
import com.example.doancuoiki.repository.UserRepository;
import com.google.firebase.auth.FirebaseUser;

import java.io.File;
import java.net.URL;
public class AccountActivity extends androidx.activity.ComponentActivity {

    // Khởi tạo các lớp xử lý dữ liệu từ Firebase
    private final AuthRepository authRepository = new AuthRepository();
    private final UserRepository userRepository = new UserRepository();

    // Khai báo các thành phần giao diện
    private ImageView avatarImage;
    private TextView profileNameText;
    private TextView profileRoleText;

    private FirebaseUser firebaseUser;
    private User currentUser;

    // Định nghĩa mã Code để nhận diện hành động chọn ảnh từ thư viện
    private static final int PICK_IMAGE_REQUEST = 999;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account);

        android.view.View mainLayout = findViewById(R.id.main_layout);
        if (mainLayout != null) {
            androidx.core.view.ViewCompat.setOnApplyWindowInsetsListener(mainLayout, (v, windowInsets) -> {
                androidx.core.graphics.Insets insets = windowInsets.getInsets(androidx.core.view.WindowInsetsCompat.Type.systemBars());
                v.setPadding(v.getPaddingLeft(), insets.top, v.getPaddingRight(), 0);
                return windowInsets;
            });
        }

        // Cấu hình thanh điều hướng Bottom Navigation
        NavigationUtils.setupBottomNav(this, NavigationUtils.ACCOUNT);

        // Ánh xạ View và cấu hình sự kiện
        bindViews();
        setupActions();
        loadProfile();
    }

    @Override
    protected void onResume() {
        super.onResume();

        // Tải lại dữ liệu mới nhất
        loadProfile();

        // Tải lại avatar khi quay từ trang Thông tin cá nhân
        loadSavedAvatar();
    }

    /**
     * Ánh xạ các biến Java với các ID tương ứng trong file Layout XML
     */
    private void bindViews() {
        avatarImage = findViewById(R.id.txtAvatar);
        profileNameText = findViewById(R.id.txtProfileName);
        profileRoleText = findViewById(R.id.txtProfileRole);
    }

    /**
     * Cấu hình tất cả các sự kiện click nút bấm trên màn hình
     */
    private void setupActions() {
        if (avatarImage != null) {
            avatarImage.setOnClickListener(v -> {
                Intent intent = new Intent(Intent.ACTION_PICK);
                intent.setType("image/*");
                startActivityForResult(intent, PICK_IMAGE_REQUEST);
            });
        }

        // 1. Bấm Thông tin cá nhân
        findViewById(R.id.btnProfileInfo).setOnClickListener(v -> {
            Intent intent = new Intent(AccountActivity.this, ProfileDetailActivity.class);
            startActivity(intent);
        });

        // 3. Bấm Quản lý nhóm
        findViewById(R.id.btnManageGroup).setOnClickListener(v ->
                Toast.makeText(AccountActivity.this, "Quản lý nhóm: Tính năng đang phát triển", Toast.LENGTH_SHORT).show()
        );

        // 4. Bấm Dự án của tôi
        findViewById(R.id.btnMyProjects).setOnClickListener(v ->
                Toast.makeText(AccountActivity.this, "Dự án của tôi: Tính năng đang phát triển", Toast.LENGTH_SHORT).show()
        );

        // 5. Bấm Cài đặt
        findViewById(R.id.btnSettings).setOnClickListener(v ->
                Toast.makeText(
                        AccountActivity.this,
                        "Cài đặt: Tính năng đang phát triển",
                        Toast.LENGTH_SHORT
                ).show()
        );

        // 6. Bấm Thông báo
        findViewById(R.id.btnNotificationSetting).setOnClickListener(v ->
                NavigationUtils.open(this, NotificationsActivity.class));

        // 7. Bấm Trợ giúp
        findViewById(R.id.btnHelp).setOnClickListener(v ->
                NavigationUtils.showMessage(this, "Trung tâm trợ giúp: đang cập nhật..."));

        // 8. Bấm Đăng xuất (Được làm sạch, không dính code thừa nữa)
        findViewById(R.id.btnLogout).setOnClickListener(v -> {
            authRepository.logout();
            NavigationUtils.openAndFinish(this, LoginActivity.class);
        });
    }

    private void loadProfile() {
        firebaseUser = authRepository.getCurrentUser();
        if (firebaseUser == null) {
            // Nếu chưa đăng nhập, đá người dùng ra màn hình Login
            NavigationUtils.openAndFinish(this, LoginActivity.class);
            return;
        }

        profileNameText.setText("Đang tải...");
        profileRoleText.setText(firebaseUser.getEmail());

        // Lấy dữ liệu chi tiết User từ Realtime Database / Firestore
        userRepository.getUser(firebaseUser.getUid(), new UserRepository.UserCallback() {
            @Override
            public void onSuccess(User user) {
                currentUser = user;
                renderProfile(user);
            }

            @Override
            public void onError(Exception exception) {
                // ĐÃ SỬA: Thêm tham số thứ 7 "" (mã nhân viên mặc định) vào cuối để khớp với Model User mới sửa
                currentUser = new User(
                        firebaseUser.getUid(),
                        valueOrDefault(firebaseUser.getEmail(), "Người dùng"),
                        valueOrDefault(firebaseUser.getEmail(), ""),
                        "Thành viên",
                        "",
                        "",
                        ""
                );
                renderProfile(currentUser);
            }
        });
    }

    /**
     * Đổ dữ liệu thông tin User lên các TextView và ImageView trên màn hình chính
     */
    private void renderProfile(User user) {
        String name = valueOrDefault(user.getName(), "Người dùng");

        profileNameText.setText(name);

        profileRoleText.setText(
                valueOrDefault(user.getRole(), "Thành viên")
                        + " - "
                        + valueOrDefault(user.getEmail(), "")
        );

        // Ưu tiên hiển thị avatar đã lưu trong bộ nhớ ứng dụng
        boolean hasLocalAvatar = loadSavedAvatar();

        // Nếu chưa có ảnh lưu trong máy thì mới dùng avatarUrl hoặc ảnh mặc định
        if (!hasLocalAvatar) {
            String avatarUrl = user.getAvatarUrl();

            if (avatarUrl != null && !avatarUrl.trim().isEmpty()) {
                loadAvatarFromUrl(avatarUrl, avatarImage);
            } else {
                avatarImage.setImageResource(
                        android.R.drawable.sym_def_app_icon
                );

                avatarImage.setBackgroundResource(
                        R.drawable.bg_card
                );
            }
        }
    }

    /**
     * Đăng xuất tài khoản và chuyển hướng về màn hình Login
     */
    private void logout() {
        authRepository.logout();
        NavigationUtils.openAndFinish(this, LoginActivity.class);
    }

    /**
     * Hàm phụ trợ: Trả về chuỗi mặc định nếu chuỗi cần kiểm tra bị rỗng hoặc null
     */
    private String valueOrDefault(String value, String defaultValue) {
        if (value == null || value.trim().isEmpty()) {
            return defaultValue;
        }
        return value.trim();
    }

    /**
     * Luồng xử lý ảnh thông minh: Phân biệt ảnh nội bộ máy (content://) và ảnh mạng trực tuyến (http)
     */
    private void loadAvatarFromUrl(String avatarUrl, ImageView imageView) {
        if (avatarUrl == null || avatarUrl.trim().isEmpty() || imageView == null) return;

        // TRƯỜNG HỢP 1: Nếu là đường dẫn nội bộ từ bộ nhớ máy (Bao gồm ảnh chọn từ máy ảo/máy thật)
        if (avatarUrl.startsWith("content://") || avatarUrl.startsWith("file://")) {
            try {
                android.net.Uri uri = android.net.Uri.parse(avatarUrl);
                java.io.InputStream inputStream = getContentResolver().openInputStream(uri);
                Bitmap bmp = BitmapFactory.decodeStream(inputStream);

                if (bmp != null) {
                    Bitmap roundBmp = getCircleBitmap(bmp);
                    runOnUiThread(() -> {
                        imageView.setImageBitmap(roundBmp);
                        imageView.setBackgroundResource(R.drawable.bg_card);
                    });
                }
            } catch (Exception e) {
                e.printStackTrace();
                runOnUiThread(() -> imageView.setImageResource(android.R.drawable.sym_def_app_icon));
            }
            return; // Dừng lại tại đây, không chạy xuống phần xử lý ảnh mạng
        }

        // TRƯỜNG HỢP 2: Nếu là ảnh link trực tuyến (HTTP/HTTPS) từ Server về
        new Thread(() -> {
            try {
                URL url = new URL(avatarUrl);
                Bitmap bmp = BitmapFactory.decodeStream(url.openConnection().getInputStream());

                if (bmp != null) {
                    Bitmap roundBmp = getCircleBitmap(bmp);
                    runOnUiThread(() -> {
                        imageView.setImageBitmap(roundBmp);
                        imageView.setBackgroundResource(R.drawable.bg_card);
                    });
                }
            } catch (Exception e) {
                runOnUiThread(() -> imageView.setImageResource(android.R.drawable.sym_def_app_icon));
            }
        }).start();
    }

    /**
     * Hàm phụ trợ: Cắt bo góc tròn xoe mịn cho tấm ảnh Bitmap
     */
    private Bitmap getCircleBitmap(Bitmap bmp) {
        int size = Math.min(bmp.getWidth(), bmp.getHeight());
        Bitmap squareBmp = Bitmap.createBitmap(bmp, (bmp.getWidth() - size) / 2, (bmp.getHeight() - size) / 2, size, size);

        Bitmap output = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(output);

        final Paint paint = new Paint();
        final Rect rect = new Rect(0, 0, size, size);

        paint.setAntiAlias(true);
        canvas.drawARGB(0, 0, 0, 0);
        canvas.drawCircle(size / 2f, size / 2f, size / 2f, paint);
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        canvas.drawBitmap(squareBmp, rect, rect, paint);
        return output;
    }
    private boolean loadSavedAvatar() {
        if (avatarImage == null) {
            return false;
        }

        String avatarPath = getSharedPreferences(
                "USER_PROFILE",
                MODE_PRIVATE
        ).getString(
                "avatar_path",
                ""
        );

        if (avatarPath == null || avatarPath.isEmpty()) {
            return false;
        }

        File avatarFile = new File(avatarPath);

        if (!avatarFile.exists()) {
            return false;
        }

        Bitmap bitmap = BitmapFactory.decodeFile(
                avatarFile.getAbsolutePath()
        );

        if (bitmap == null) {
            return false;
        }

        Bitmap roundBitmap = getCircleBitmap(bitmap);

        avatarImage.setImageBitmap(roundBitmap);
        avatarImage.setBackgroundResource(R.drawable.bg_card);

        return true;
    }
}