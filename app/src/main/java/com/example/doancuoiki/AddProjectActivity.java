package com.example.doancuoiki;

import android.app.Activity;
import android.os.Bundle;
import android.widget.EditText;

public class AddProjectActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_project);

        EditText nameInput = findViewById(R.id.edtProjectName);

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
        findViewById(R.id.btnCreateProject).setOnClickListener(v -> {
            if (nameInput.getText().toString().trim().isEmpty()) {
                NavigationUtils.showDeveloping(this, "Kiểm tra tên dự án");
                return;
            }

            // TODO Bạn 2: lưu project vào Firestore collection projects.
            NavigationUtils.showDeveloping(this, "Lưu dự án lên Firebase");
        });
    }
}
