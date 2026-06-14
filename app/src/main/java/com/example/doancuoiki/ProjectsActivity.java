package com.example.doancuoiki;

import android.app.Activity;
import android.os.Bundle;
import android.widget.LinearLayout;

public class ProjectsActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_projects);
        NavigationUtils.setupBottomNav(this, NavigationUtils.PROJECTS);

        LinearLayout projectList = findViewById(R.id.projectList);
        for (int i = 0; i < SampleData.PROJECT_NAMES.length; i++) {
            projectList.addView(ViewFactory.projectCard(
                    this,
                    SampleData.PROJECT_NAMES[i],
                    SampleData.PROJECT_TASKS[i] + " công việc - " + SampleData.PROJECT_PROGRESS[i] + "%",
                    SampleData.PROJECT_PROGRESS[i],
                    v -> NavigationUtils.open(this, ProjectDetailActivity.class)
            ));
        }

        findViewById(R.id.btnOpenAddProject).setOnClickListener(v ->
                NavigationUtils.open(this, AddProjectActivity.class));
        findViewById(R.id.edtSearchProject).setOnClickListener(v ->
                NavigationUtils.showDeveloping(this, "Tìm kiếm dự án"));
    }
}
