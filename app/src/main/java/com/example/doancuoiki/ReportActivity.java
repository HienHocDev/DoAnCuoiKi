package com.example.doancuoiki;

import android.app.Activity;
import android.os.Bundle;
import android.widget.LinearLayout;

public class ReportActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_report);

        LinearLayout reportList = findViewById(R.id.reportProjectList);
        for (int i = 0; i < SampleData.PROJECT_NAMES.length; i++) {
            reportList.addView(ViewFactory.projectCard(
                    this,
                    SampleData.PROJECT_NAMES[i],
                    SampleData.PROJECT_PROGRESS[i] + "%",
                    SampleData.PROJECT_PROGRESS[i],
                    v -> NavigationUtils.showDeveloping(this, "Biểu đồ chi tiết")
            ));
        }

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
        findViewById(R.id.btnPerformance).setOnClickListener(v ->
                NavigationUtils.showDeveloping(this, "Hiệu suất thành viên"));
    }
}
