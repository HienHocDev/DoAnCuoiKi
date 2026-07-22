package com.example.doancuoiki;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.ComponentActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class HelpActivity extends ComponentActivity {

    private ImageView btnBack;
    private EditText edtSupportSubject;
    private EditText edtSupportMessage;
    private Button btnSendSupport;
    private TextView txtSupportStatus;

    private FirebaseFirestore firestore;
    private FirebaseUser firebaseUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_help);

        firestore = FirebaseFirestore.getInstance();
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        bindViews();
        setupActions();
    }

    private void bindViews() {
        btnBack = findViewById(R.id.btnBack);
        edtSupportSubject = findViewById(R.id.edtSupportSubject);
        edtSupportMessage = findViewById(R.id.edtSupportMessage);
        btnSendSupport = findViewById(R.id.btnSendSupport);
        txtSupportStatus = findViewById(R.id.txtSupportStatus);
    }

    private void setupActions() {
        btnBack.setOnClickListener(v -> finish());

        btnSendSupport.setOnClickListener(v -> sendSupportRequest());
    }

    private void sendSupportRequest() {
        if (firebaseUser == null) {
            Toast.makeText(
                    this,
                    "Bạn cần đăng nhập để gửi yêu cầu hỗ trợ",
                    Toast.LENGTH_SHORT
            ).show();
            return;
        }

        String subject = edtSupportSubject.getText().toString().trim();
        String message = edtSupportMessage.getText().toString().trim();

        if (subject.isEmpty()) {
            edtSupportSubject.setError("Vui lòng nhập tiêu đề");
            edtSupportSubject.requestFocus();
            return;
        }

        if (message.isEmpty()) {
            edtSupportMessage.setError("Vui lòng mô tả vấn đề");
            edtSupportMessage.requestFocus();
            return;
        }

        btnSendSupport.setEnabled(false);
        btnSendSupport.setText("ĐANG GỬI...");
        txtSupportStatus.setText("");

        Map<String, Object> supportData = new HashMap<>();
        supportData.put("userId", firebaseUser.getUid());
        supportData.put("email", firebaseUser.getEmail());
        supportData.put("subject", subject);
        supportData.put("message", message);
        supportData.put("status", "pending");
        supportData.put("createdAt", FieldValue.serverTimestamp());

        firestore.collection("support_requests")
                .add(supportData)
                .addOnSuccessListener(documentReference -> {
                    btnSendSupport.setEnabled(true);
                    btnSendSupport.setText("GỬI YÊU CẦU");

                    edtSupportSubject.setText("");
                    edtSupportMessage.setText("");

                    txtSupportStatus.setText(
                            "Đã gửi yêu cầu thành công. Mã yêu cầu: "
                                    + documentReference.getId()
                    );

                    Toast.makeText(
                            HelpActivity.this,
                            "Yêu cầu đã được gửi",
                            Toast.LENGTH_SHORT
                    ).show();
                })
                .addOnFailureListener(exception -> {
                    btnSendSupport.setEnabled(true);
                    btnSendSupport.setText("GỬI YÊU CẦU");

                    txtSupportStatus.setText("");

                    Toast.makeText(
                            HelpActivity.this,
                            "Không gửi được yêu cầu: "
                                    + exception.getMessage(),
                            Toast.LENGTH_LONG
                    ).show();
                });
    }
}