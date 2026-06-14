package com.example.doancuoiki;

import android.app.Activity;
import android.os.Bundle;
import android.widget.EditText;

public class LoginActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        EditText emailInput = findViewById(R.id.edtEmail);
        EditText passwordInput = findViewById(R.id.edtPassword);

        findViewById(R.id.btnLogin).setOnClickListener(v -> {
            String email = emailInput.getText().toString().trim();
            String password = passwordInput.getText().toString().trim();

            if (email.isEmpty() || password.isEmpty()) {
                NavigationUtils.showDeveloping(this, "Kiểm tra email/mật khẩu");
                return;
            }

            // TODO Bạn 1: thay phần này bằng FirebaseAuth.signInWithEmailAndPassword.
            NavigationUtils.open(this, HomeActivity.class);
        });

        findViewById(R.id.txtForgotPassword).setOnClickListener(v ->
                NavigationUtils.showDeveloping(this, "Quên mật khẩu"));
        findViewById(R.id.txtRegister).setOnClickListener(v ->
                NavigationUtils.showDeveloping(this, "Đăng ký tài khoản"));
    }
}
