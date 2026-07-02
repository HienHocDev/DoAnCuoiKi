package com.example.doancuoiki;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.example.doancuoiki.model.Project;
import com.example.doancuoiki.repository.ProjectRepository;
import com.example.doancuoiki.utils.DateUtils;
import com.example.doancuoiki.utils.VietnameseInputUtils;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class AddProjectActivity extends Activity {
    private final ProjectRepository projectRepository = new ProjectRepository();

    private EditText nameInput;
    private EditText descriptionInput;
    private EditText startDateInput;
    private EditText endDateInput;
    private TextView createStateText;
    private TextView createButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_project);
        android.view.View mainLayout = findViewById(R.id.main_layout);
        if (mainLayout != null) {
            androidx.core.view.ViewCompat.setOnApplyWindowInsetsListener(mainLayout, (v, windowInsets) -> {
                androidx.core.graphics.Insets insets = windowInsets.getInsets(androidx.core.view.WindowInsetsCompat.Type.systemBars());
                v.setPadding(v.getPaddingLeft(), insets.top, v.getPaddingRight(), insets.bottom);
                return windowInsets;
            });
        }

        nameInput = findViewById(R.id.edtProjectName);
        descriptionInput = findViewById(R.id.edtProjectDescription);
        startDateInput = findViewById(R.id.edtStartDate);
        endDateInput = findViewById(R.id.edtEndDate);
        createStateText = findViewById(R.id.txtCreateState);
        createButton = findViewById(R.id.btnCreateProject);

        VietnameseInputUtils.setupSingleLine(nameInput);
        VietnameseInputUtils.setupMultiLine(descriptionInput);

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
        createButton.setOnClickListener(v -> validateAndCreateProject());
        startDateInput.setOnClickListener(v -> showDatePicker(startDateInput));
        endDateInput.setOnClickListener(v -> showDatePicker(endDateInput));
    }

    private void validateAndCreateProject() {
        String name = nameInput.getText().toString().trim();
        String startDate = DateUtils.normalize(startDateInput.getText().toString());
        String endDate = DateUtils.normalize(endDateInput.getText().toString());

        if (name.isEmpty()) {
            showState("Vui lòng nhập tên dự án");
            return;
        }
        if (startDate.isEmpty() || endDate.isEmpty()) {
            showState("Vui lòng chọn đầy đủ ngày bắt đầu và kết thúc");
            return;
        }
        if (isEndBeforeStart(startDate, endDate)) {
            showState("Ngày kết thúc phải sau hoặc bằng ngày bắt đầu");
            return;
        }

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            showState("Bạn cần đăng nhập trước khi tạo dự án");
            return;
        }

        List<String> members = new ArrayList<>();
        members.add(currentUser.getUid());
        createProject(currentUser.getUid(), members, startDate, endDate);
    }

    private void createProject(String ownerId, List<String> members, String startDate, String endDate) {
        setLoading(true, "Đang tạo dự án...");
        String now = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(new Date());
        Project project = new Project(
                null,
                nameInput.getText().toString().trim(),
                descriptionInput.getText().toString().trim(),
                startDate,
                endDate,
                0,
                ownerId,
                members,
                now,
                now
        );

        projectRepository.addProject(project, new ProjectRepository.SimpleCallback() {
            @Override
            public void onSuccess() {
                NavigationUtils.showMessage(AddProjectActivity.this, "Đã tạo dự án");
                finish();
            }

            @Override
            public void onError(Exception exception) {
                setLoading(false, "Tạo dự án thất bại: " + exception.getMessage());
            }
        });
    }

    private void showDatePicker(EditText targetInput) {
        Calendar calendar = Calendar.getInstance();
        Date existingDate = parseDate(targetInput.getText().toString());
        if (existingDate != null) {
            calendar.setTime(existingDate);
        }
        new DatePickerDialog(
                this,
                (view, year, month, day) ->
                        targetInput.setText(DateUtils.fromCalendarDate(year, month, day)),
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
        ).show();
    }

    private boolean isEndBeforeStart(String startDate, String endDate) {
        Date start = parseDate(startDate);
        Date end = parseDate(endDate);
        return start != null && end != null && end.before(start);
    }

    private Date parseDate(String value) {
        String normalized = DateUtils.normalize(value);
        if (normalized.isEmpty()) {
            return null;
        }
        try {
            SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            formatter.setLenient(false);
            return formatter.parse(normalized);
        } catch (ParseException exception) {
            return null;
        }
    }

    private void setLoading(boolean loading, String message) {
        createButton.setEnabled(!loading);
        createButton.setText(loading ? "Đang xử lý..." : "Tạo dự án");
        showState(message);
    }

    private void showState(String message) {
        createStateText.setText(message);
    }
}
