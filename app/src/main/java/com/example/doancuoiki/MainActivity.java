package com.example.doancuoiki;

import android.app.Activity;
import android.os.Bundle;

public class MainActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        NavigationUtils.open(this, LoginActivity.class);
        finish();
    }
}
