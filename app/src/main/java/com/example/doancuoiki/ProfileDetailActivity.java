package com.example.doancuoiki;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.doancuoiki.model.User;
import com.example.doancuoiki.repository.AuthRepository;
import com.example.doancuoiki.repository.UserRepository;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

public class ProfileDetailActivity extends androidx.activity.ComponentActivity {

    private final AuthRepository authRepository = new AuthRepository();
    private final UserRepository userRepository = new UserRepository();

    private ImageView btnBack, detailAvatar;
    private TextView txtProfileName;
    private TextView txtProfileEmail;
    private EditText editName, editPhone, editAddress;
    private Button btnSaveProfile;

    private FirebaseUser firebaseUser;
    private User currentUser;
    private static final int PICK_IMAGE_DETAIL_REQUEST = 888;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_detail);

        // 1. Ánh xạ View từ layout của bạn
        btnBack = findViewById(R.id.btnBack);
        detailAvatar = findViewById(R.id.detailAvatar);
        txtProfileName = findViewById(R.id.txtProfileName);
        txtProfileEmail = findViewById(R.id.txtProfileRole);
        editName = findViewById(R.id.editName);
        editPhone = findViewById(R.id.editPhone);
        editAddress = findViewById(R.id.editAddress);
        btnSaveProfile = findViewById(R.id.btnSaveProfile);

        // 2. Sự kiện nút quay lại và chọn ảnh
        if (btnBack != null) btnBack.setOnClickListener(v -> finish());
        if (detailAvatar != null) {
            detailAvatar.setOnClickListener(v -> {
                Intent intent = new Intent(Intent.ACTION_PICK);
                intent.setType("image/*");
                startActivityForResult(intent, PICK_IMAGE_DETAIL_REQUEST);
            });
        }

        // 3. Tải thông tin từ Cloud Firestore về hiển thị lên màn hình
        firebaseUser = authRepository.getCurrentUser();
        if (firebaseUser != null) {
            if (txtProfileEmail != null) {
                txtProfileEmail.setText(firebaseUser.getEmail());
            }

            // Đọc trực tiếp data SĐT và Địa chỉ từ Document Firestore để tránh lỗi model
            FirebaseFirestore.getInstance().collection("users")
                    .document(firebaseUser.getUid())
                    .get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            String phone = documentSnapshot.getString("phone");
                            String address = documentSnapshot.getString("address");

                            if (editPhone != null && phone != null) editPhone.setText(phone);
                            if (editAddress != null && address != null) editAddress.setText(address);
                        }
                    });

            // Sử dụng repo để lấy Tên hiển thị cũ của bạn
            userRepository.getUser(firebaseUser.getUid(), new UserRepository.UserCallback() {
                @Override
                public void onSuccess(User user) {
                    currentUser = user;
                    if (user != null) {
                        if (txtProfileName != null) txtProfileName.setText(user.getName());
                        if (editName != null) editName.setText(user.getName());

                        if (txtProfileEmail != null) {
                            if (user.getEmail() != null && !user.getEmail().isEmpty()) {
                                txtProfileEmail.setText(user.getEmail());
                            } else {
                                txtProfileEmail.setText(firebaseUser.getEmail());
                            }
                        }
                    }
                }
                @Override
                public void onError(Exception exception) {
                    Toast.makeText(ProfileDetailActivity.this, "Lỗi tải thông tin tên!", Toast.LENGTH_SHORT).show();
                }
            });
        }

        // 4. Sự kiện bấm nút LƯU -> Thực hiện cập nhật dữ liệu bằng Firestore Repo mới
        if (btnSaveProfile != null) {
            btnSaveProfile.setOnClickListener(v -> {
                if (firebaseUser == null) return;

                String updatedName = editName != null ? editName.getText().toString().trim() : "";
                String updatedPhone = editPhone != null ? editPhone.getText().toString().trim() : "";
                String updatedAddress = editAddress != null ? editAddress.getText().toString().trim() : "";

                if (updatedName.isEmpty()) {
                    Toast.makeText(this, "Họ tên không được trống!", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Gọi hàm Firestore để lưu Số điện thoại và Địa chỉ
                userRepository.updatePhoneAndAddress(firebaseUser.getUid(), updatedPhone, updatedAddress, new UserRepository.SimpleCallback() {
                    @Override
                    public void onSuccess() {
                        // Sau khi lưu thành công SĐT và địa chỉ, tiến hành cập nhật nốt Tên của bạn
                        userRepository.updateName(firebaseUser.getUid(), updatedName, new UserRepository.SimpleCallback() {
                            @Override
                            public void onSuccess() {
                                Toast.makeText(ProfileDetailActivity.this, "Cập nhật thông tin thành công!", Toast.LENGTH_SHORT).show();
                                if (txtProfileName != null) txtProfileName.setText(updatedName);
                                finish();
                            }

                            @Override
                            public void onError(Exception exception) {
                                Toast.makeText(ProfileDetailActivity.this, "Lỗi lưu tên: " + exception.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });
                    }

                    @Override
                    public void onError(Exception exception) {
                        Toast.makeText(ProfileDetailActivity.this, "Lỗi lưu SĐT và Địa chỉ!", Toast.LENGTH_SHORT).show();
                    }
                });
            });
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_DETAIL_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            Uri selectedImageUri = data.getData();
            if (detailAvatar != null) {
                detailAvatar.setImageURI(selectedImageUri);
            }
        }
    }
}