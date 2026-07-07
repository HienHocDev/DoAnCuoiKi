package com.example.doancuoiki;

import android.app.Activity;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import com.example.doancuoiki.repository.AuthRepository;

public class ChangePasswordActivity extends Activity {

    private final AuthRepository authRepository = new AuthRepository();

    private EditText edtCurrentPassword;
    private EditText edtNewPassword;
    private EditText edtConfirmPassword;
    private Button btnConfirm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_password); // Đảm bảo đúng tên file layout XML của giao diện 3 ô

        // 1. Ánh xạ các View từ Layout XML
        edtCurrentPassword = findViewById(R.id.edtCurrentPassword); // Thay đúng ID ô Mật khẩu hiện tại
        edtNewPassword = findViewById(R.id.edtNewPassword);         // Thay đúng ID ô Mật khẩu mới
        edtConfirmPassword = findViewById(R.id.edtConfirmPassword); // Thay đúng ID ô Nhập lại mật khẩu mới
        btnConfirm = findViewById(R.id.btnConfirm);                 // Thay đúng ID nút Xác nhận

        // Nút quay lại trên thanh tiêu đề
       findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        // 2. Xử lý sự kiện khi bấm nút Xác nhận
        if (btnConfirm != null) {
            btnConfirm.setOnClickListener(v -> {
                String currentPass = edtCurrentPassword.getText().toString().trim();
                String newPass = edtNewPassword.getText().toString().trim();
                String confirmPass = edtConfirmPassword.getText().toString().trim();

                if (currentPass.isEmpty() || newPass.isEmpty() || confirmPass.isEmpty()) {
                    NavigationUtils.showMessage(this, "Vui lòng nhập đầy đủ thông tin");
                    return;
                }

                if (newPass.length() < 6) {
                    NavigationUtils.showMessage(this, "Mật khẩu mới phải từ 6 ký tự trở lên");
                    return;
                }

                if (!newPass.equals(confirmPass)) {
                    NavigationUtils.showMessage(this, "Mật khẩu nhập lại không trùng khớp");
                    return;
                }

                // Gọi hàm xác thực và cập nhật trực tiếp từ AuthRepository
                authRepository.changePasswordDirectly(currentPass, newPass, new AuthRepository.SimpleCallback() {
                    @Override
                    public void onSuccess() {
                        NavigationUtils.showMessage(ChangePasswordActivity.this, "Đổi mật khẩu thành công!");
                        finish();
                    }

                    @Override
                    public void onError(Exception exception) {
                        NavigationUtils.showMessage(ChangePasswordActivity.this, "Lỗi: " + exception.getMessage());
                    }
                });
            });
        }
    }
}