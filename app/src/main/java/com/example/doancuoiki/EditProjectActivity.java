package com.example.doancuoiki;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
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
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class EditProjectActivity extends Activity {
    public static final String EXTRA_PROJECT_ID = "editProjectId";
    public static final String EXTRA_PROJECT_NAME = "editProjectName";
    public static final String EXTRA_PROJECT_DESC = "editProjectDesc";
    public static final String EXTRA_PROJECT_START = "editProjectStart";
    public static final String EXTRA_PROJECT_END = "editProjectEnd";
    public static final String EXTRA_OWNER_ID = "editProjectOwnerId";

    private final ProjectRepository projectRepository = new ProjectRepository();

    private EditText nameInput;
    private EditText descriptionInput;
    private EditText startDateInput;
    private EditText endDateInput;
    private TextView stateText;
    private TextView saveButton;

    private String projectId;
    private String ownerId;
    private String currentUserId = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_project);

        android.view.View mainLayout = findViewById(R.id.main_layout);
        if (mainLayout != null) {
            androidx.core.view.ViewCompat.setOnApplyWindowInsetsListener(mainLayout, (v, windowInsets) -> {
                androidx.core.graphics.Insets insets = windowInsets.getInsets(androidx.core.view.WindowInsetsCompat.Type.systemBars());
                v.setPadding(v.getPaddingLeft(), insets.top, v.getPaddingRight(), insets.bottom);
                return windowInsets;
            });
        }

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        currentUserId = user == null ? "" : user.getUid();

        projectId = getIntent().getStringExtra(EXTRA_PROJECT_ID);
        ownerId = getIntent().getStringExtra(EXTRA_OWNER_ID);

        if (!currentUserId.equals(ownerId)) {
            NavigationUtils.showMessage(this, "Chỉ chủ dự án được chỉnh sửa");
            finish();
            return;
        }

        bindViews();
        prefillData();

        VietnameseInputUtils.setupSingleLine(nameInput);
        VietnameseInputUtils.setupMultiLine(descriptionInput);

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
        saveButton.setOnClickListener(v -> validateAndSave());
        startDateInput.setOnClickListener(v -> showDatePicker(startDateInput));
        endDateInput.setOnClickListener(v -> showDatePicker(endDateInput));
    }

    private void bindViews() {
        nameInput = findViewById(R.id.edtProjectName);
        descriptionInput = findViewById(R.id.edtProjectDescription);
        startDateInput = findViewById(R.id.edtStartDate);
        endDateInput = findViewById(R.id.edtEndDate);
        stateText = findViewById(R.id.txtEditState);
        saveButton = findViewById(R.id.btnSaveProject);
    }

    private void prefillData() {
        Intent intent = getIntent();
        nameInput.setText(intent.getStringExtra(EXTRA_PROJECT_NAME));
        descriptionInput.setText(intent.getStringExtra(EXTRA_PROJECT_DESC));
        startDateInput.setText(intent.getStringExtra(EXTRA_PROJECT_START));
        endDateInput.setText(intent.getStringExtra(EXTRA_PROJECT_END));
    }

    private void validateAndSave() {
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

        setLoading(true);

        // Load current project to preserve fields like members, progress, ownerId
        projectRepository.getProjectById(projectId, new ProjectRepository.ProjectCallback() {
            @Override
            public void onSuccess(Project project) {
                project.setName(name);
                project.setDescription(descriptionInput.getText().toString().trim());
                project.setStartDate(startDate);
                project.setEndDate(endDate);
                String now = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(new Date());
                project.setUpdatedAt(now);

                projectRepository.updateProject(project, new ProjectRepository.SimpleCallback() {
                    @Override
                    public void onSuccess() {
                        // Log activity
                        com.example.doancuoiki.model.ActivityLog log = new com.example.doancuoiki.model.ActivityLog();
                        log.setProjectId(projectId);
                        log.setUserId(currentUserId);
                        log.setUserName("Chủ dự án");
                        log.setActionText("đã cập nhật dự án");
                        log.setTargetName(name);
                        log.setType("project");
                        log.setTimestamp(new com.google.firebase.Timestamp(new Date()));
                        projectRepository.addActivityLog(log, null);

                        NavigationUtils.showMessage(EditProjectActivity.this, "Đã lưu thay đổi");
                        setResult(RESULT_OK);
                        finish();
                    }

                    @Override
                    public void onError(Exception exception) {
                        setLoading(false);
                        showState("Lưu thất bại: " + exception.getMessage());
                    }
                });
            }

            @Override
            public void onError(Exception exception) {
                setLoading(false);
                showState("Không tải được dự án: " + exception.getMessage());
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
        if (normalized.isEmpty()) return null;
        try {
            SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            formatter.setLenient(false);
            return formatter.parse(normalized);
        } catch (ParseException e) {
            return null;
        }
    }

    private void setLoading(boolean loading) {
        saveButton.setEnabled(!loading);
        saveButton.setText(loading ? "Đang lưu..." : "Lưu thay đổi");
    }

    private void showState(String message) {
        stateText.setText(message);
    }
}
