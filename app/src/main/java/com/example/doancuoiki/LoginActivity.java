package com.example.doancuoiki;

import android.app.Activity;
import android.os.Bundle;
import android.widget.EditText;

import com.example.doancuoiki.repository.AuthRepository;
import com.google.firebase.auth.FirebaseUser;

public class LoginActivity extends Activity {
    private final AuthRepository authRepository = new AuthRepository();

    private EditText emailInput;
    private EditText passwordInput;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (authRepository.getCurrentUser() != null) {
            NavigationUtils.openAndFinish(this, HomeActivity.class);
            return;
        }

        setContentView(R.layout.activity_login);
        bindViews();
        setupActions();
    }

    private void bindViews() {
        emailInput = findViewById(R.id.edtEmail);
        passwordInput = findViewById(R.id.edtPassword);
    }

    private void setupActions() {
        findViewById(R.id.btnLogin).setOnClickListener(v -> login());
        findViewById(R.id.txtForgotPassword).setOnClickListener(v -> sendPasswordReset());
        findViewById(R.id.txtRegister).setOnClickListener(v ->
                NavigationUtils.open(this, RegisterActivity.class));
    }

    private void login() {
        String email = emailInput.getText().toString().trim();
        String password = passwordInput.getText().toString().trim();

        if (email.isEmpty() || password.isEmpty()) {
            NavigationUtils.showMessage(this, "Vui lòng nhập email và mật khẩu");
            return;
        }

        authRepository.login(email, password, new AuthRepository.AuthCallback() {
            @Override
            public void onSuccess(FirebaseUser firebaseUser) {
                NavigationUtils.showMessage(LoginActivity.this, "Đăng nhập thành công");
                NavigationUtils.openAndFinish(LoginActivity.this, HomeActivity.class);
            }

            @Override
            public void onError(Exception exception) {
                NavigationUtils.showMessage(LoginActivity.this, "Đăng nhập thất bại: " + exception.getMessage());
            }
        });
    }

    private void sendPasswordReset() {
        String email = emailInput.getText().toString().trim();
        if (email.isEmpty()) {
            NavigationUtils.showMessage(this, "Nhập email để đặt lại mật khẩu");
            return;
        }

        authRepository.sendPasswordReset(email, new AuthRepository.SimpleCallback() {
            @Override
            public void onSuccess() {
                NavigationUtils.showMessage(LoginActivity.this, "Đã gửi email đặt lại mật khẩu");
            }

            @Override
            public void onError(Exception exception) {
                NavigationUtils.showMessage(LoginActivity.this, "Không gửi được email: " + exception.getMessage());
            }
        });
    }
}
