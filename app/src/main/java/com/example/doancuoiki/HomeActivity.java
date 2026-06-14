package com.example.doancuoiki;

import android.app.Activity;
import android.os.Bundle;
import android.widget.LinearLayout;

public class HomeActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        NavigationUtils.setupBottomNav(this, NavigationUtils.HOME);

        LinearLayout projectList = findViewById(R.id.homeProjectList);
        for (int i = 0; i < SampleData.PROJECT_NAMES.length; i++) {
            projectList.addView(ViewFactory.projectCard(
                    this,
                    SampleData.PROJECT_NAMES[i],
                    SampleData.PROJECT_TASKS[i] + " công việc - " + SampleData.PROJECT_PROGRESS[i] + "%",
                    SampleData.PROJECT_PROGRESS[i],
                    v -> NavigationUtils.open(this, ProjectDetailActivity.class)
            ));
        }

        findViewById(R.id.btnAddProject).setOnClickListener(v ->
                NavigationUtils.open(this, AddProjectActivity.class));
    }
}
