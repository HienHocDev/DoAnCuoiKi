package com.example.doancuoiki;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.example.doancuoiki.model.Project;
import com.example.doancuoiki.repository.ProjectRepository;
import com.example.doancuoiki.repository.UserRepository;
import com.example.doancuoiki.utils.DateUtils;
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
    private final UserRepository userRepository = new UserRepository();

    private EditText nameInput;
    private EditText descriptionInput;
    private EditText startDateInput;
    private EditText endDateInput;
    private EditText membersInput;
    private TextView createStateText;
    private Button createButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_project);

        bindViews();
        setupActions();
    }

    private void bindViews() {
        nameInput = findViewById(R.id.edtProjectName);
        descriptionInput = findViewById(R.id.edtProjectDescription);
        startDateInput = findViewById(R.id.edtStartDate);
        endDateInput = findViewById(R.id.edtEndDate);
        membersInput = findViewById(R.id.edtMembers);
        createStateText = findViewById(R.id.txtCreateState);
        createButton = findViewById(R.id.btnCreateProject);
    }

    private void setupActions() {
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

        if (startDate.isEmpty()) {
            showState("Vui lòng chọn ngày bắt đầu");
            return;
        }

        if (endDate.isEmpty()) {
            showState("Vui lòng chọn ngày kết thúc");
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

        setLoading(true, "Đang kiểm tra thành viên...");
        userRepository.resolveMemberIds(parseMemberInputs(), currentUser.getUid(), new UserRepository.MemberResolveCallback() {
            @Override
            public void onSuccess(List<String> userIds, List<String> unresolvedMembers) {
                if (!unresolvedMembers.isEmpty()) {
                    setLoading(false, "Không tìm thấy thành viên: " + join(unresolvedMembers));
                    return;
                }
                createProject(currentUser.getUid(), userIds, startDate, endDate);
            }

            @Override
            public void onError(Exception exception) {
                setLoading(false, "Không kiểm tra được thành viên");
            }
        });
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
                (view, year, month, dayOfMonth) ->
                        targetInput.setText(DateUtils.fromCalendarDate(year, month, dayOfMonth)),
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
        ).show();
    }

    private List<String> parseMemberInputs() {
        List<String> members = new ArrayList<>();
        String rawMembers = membersInput.getText().toString();
        if (rawMembers.isEmpty()) {
            return members;
        }
        String[] parts = rawMembers.split(",");
        for (String part : parts) {
            String member = part.trim();
            if (!member.isEmpty() && !members.contains(member)) {
                members.add(member);
            }
        }
        return members;
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

    private String join(List<String> values) {
        StringBuilder builder = new StringBuilder();
        for (String value : values) {
            if (builder.length() > 0) {
                builder.append(", ");
            }
            builder.append(value);
        }
        return builder.toString();
    }
}
