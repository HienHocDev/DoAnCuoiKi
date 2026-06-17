package com.example.doancuoiki;

import android.app.Activity;
import android.os.Bundle;
import android.widget.EditText;

import com.example.doancuoiki.repository.AuthRepository;
import com.google.firebase.auth.FirebaseUser;

public class RegisterActivity extends Activity {
    private final AuthRepository authRepository = new AuthRepository();

    private EditText nameInput;
    private EditText emailInput;
    private EditText passwordInput;
    private EditText confirmPasswordInput;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        bindViews();
        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
        findViewById(R.id.btnRegister).setOnClickListener(v -> register());
    }

    private void bindViews() {
        nameInput = findViewById(R.id.edtRegisterName);
        emailInput = findViewById(R.id.edtRegisterEmail);
        passwordInput = findViewById(R.id.edtRegisterPassword);
        confirmPasswordInput = findViewById(R.id.edtRegisterConfirmPassword);
    }

    private void register() {
        String name = nameInput.getText().toString().trim();
        String email = emailInput.getText().toString().trim();
        String password = passwordInput.getText().toString().trim();
        String confirmPassword = confirmPasswordInput.getText().toString().trim();

        if (name.isEmpty() || email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
            NavigationUtils.showMessage(this, "Vui lòng nhập đầy đủ thông tin");
            return;
        }

        if (password.length() < 6) {
            NavigationUtils.showMessage(this, "Mật khẩu cần ít nhất 6 ký tự");
            return;
        }

        if (!password.equals(confirmPassword)) {
            NavigationUtils.showMessage(this, "Mật khẩu nhập lại chưa khớp");
            return;
        }

        authRepository.register(name, email, password, new AuthRepository.AuthCallback() {
            @Override
            public void onSuccess(FirebaseUser firebaseUser) {
                NavigationUtils.showMessage(RegisterActivity.this, "Đăng ký thành công");
                NavigationUtils.openAndFinish(RegisterActivity.this, HomeActivity.class);
            }

            @Override
            public void onError(Exception exception) {
                NavigationUtils.showMessage(RegisterActivity.this, "Đăng ký thất bại: " + exception.getMessage());
            }
        });
    }
}
