package com.example.doancuoiki;

import android.app.Activity;
import android.os.Bundle;
import android.widget.EditText;

import com.example.doancuoiki.model.Project;
import com.example.doancuoiki.repository.ProjectRepository;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class AddProjectActivity extends Activity {
    private static final String GUEST_USER_ID = "guest";

    private final ProjectRepository projectRepository = new ProjectRepository();

    private EditText nameInput;
    private EditText descriptionInput;
    private EditText startDateInput;
    private EditText endDateInput;
    private EditText membersInput;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_project);

        bindViews();
        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
        findViewById(R.id.btnCreateProject).setOnClickListener(v -> createProject());
    }

    private void bindViews() {
        nameInput = findViewById(R.id.edtProjectName);
        descriptionInput = findViewById(R.id.edtProjectDescription);
        startDateInput = findViewById(R.id.edtStartDate);
        endDateInput = findViewById(R.id.edtEndDate);
        membersInput = findViewById(R.id.edtMembers);
    }

    private void createProject() {
        String name = nameInput.getText().toString().trim();
        if (name.isEmpty()) {
            NavigationUtils.showMessage(this, "Vui lòng nhập tên dự án");
            return;
        }

        String ownerId = currentUserId();
        List<String> members = parseMembers(membersInput.getText().toString(), ownerId);
        String now = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(new Date());

        Project project = new Project(
                null,
                name,
                descriptionInput.getText().toString().trim(),
                startDateInput.getText().toString().trim(),
                endDateInput.getText().toString().trim(),
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
                NavigationUtils.showMessage(AddProjectActivity.this, "Tạo dự án thất bại: " + exception.getMessage());
            }
        });
    }

    private List<String> parseMembers(String rawMembers, String ownerId) {
        List<String> members = new ArrayList<>();
        members.add(ownerId);

        String[] parts = rawMembers.split(",");
        for (String part : parts) {
            String member = part.trim();
            if (!member.isEmpty() && !members.contains(member)) {
                members.add(member);
            }
        }

        return members;
    }

    private String currentUserId() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            return GUEST_USER_ID;
        }
        return user.getUid();
    }
}
