package com.example.doancuoiki;

import android.app.Activity;
import android.os.Bundle;

public class AccountActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account);
        NavigationUtils.setupBottomNav(this, NavigationUtils.ACCOUNT);

        findViewById(R.id.btnProfileInfo).setOnClickListener(v ->
                NavigationUtils.showDeveloping(this, "Thông tin cá nhân"));
        findViewById(R.id.btnChangePassword).setOnClickListener(v ->
                NavigationUtils.showDeveloping(this, "Đổi mật khẩu"));
        findViewById(R.id.btnNotificationSetting).setOnClickListener(v ->
                NavigationUtils.showDeveloping(this, "Cài đặt thông báo"));
        findViewById(R.id.btnLanguage).setOnClickListener(v ->
                NavigationUtils.showDeveloping(this, "Ngôn ngữ"));
        findViewById(R.id.btnHelp).setOnClickListener(v ->
                NavigationUtils.showDeveloping(this, "Trợ giúp"));
        findViewById(R.id.btnLogout).setOnClickListener(v ->
                NavigationUtils.open(this, LoginActivity.class));
    }
}
