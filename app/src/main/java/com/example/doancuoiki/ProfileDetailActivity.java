package com.example.doancuoiki;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
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

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class ProfileDetailActivity extends androidx.activity.ComponentActivity {

    private final AuthRepository authRepository = new AuthRepository();
    private final UserRepository userRepository = new UserRepository();

    private ImageView btnBack, detailAvatar;
    private TextView txtProfileName;
    private TextView txtProfileEmail;

    private EditText editName, editPhone, editAddress, editEmployeeCode, editBirthDate;
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
        editEmployeeCode = findViewById(R.id.editEmployeeCode);
        editBirthDate = findViewById(R.id.editBirthDate);
        btnSaveProfile = findViewById(R.id.btnSaveProfile);

        // Tải lại ảnh đại diện đã lưu khi mở màn hình
        loadSavedAvatar();

        // KHOÁ KHÔNG CHO GÕ CHỮ VÀO Ô NGÀY SINH ĐỂ BẮT BUỘC CHỌN TỪ LỊCH
        if (editBirthDate != null) {
            editBirthDate.setFocusable(false);
            editBirthDate.setClickable(true);

            editBirthDate.setOnClickListener(v -> {
                Calendar calendar = Calendar.getInstance();
                int year = calendar.get(Calendar.YEAR);
                int month = calendar.get(Calendar.MONTH);
                int day = calendar.get(Calendar.DAY_OF_MONTH);

                DatePickerDialog datePickerDialog = new DatePickerDialog(
                        ProfileDetailActivity.this,
                        (view, selectedYear, selectedMonth, selectedDay) -> {
                            String formattedDate = String.format(
                                    "%02d/%02d/%d",
                                    selectedDay,
                                    selectedMonth + 1,
                                    selectedYear
                            );

                            editBirthDate.setText(formattedDate);
                        },
                        year,
                        month,
                        day
                );

                datePickerDialog.show();
            });
        }


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

            // Đọc dữ liệu SĐT, Địa chỉ, Mã nhân viên, Ngày sinh từ Firestore
            FirebaseFirestore.getInstance().collection("users")
                    .document(firebaseUser.getUid())
                    .get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            String phone = documentSnapshot.getString("phone");
                            String address = documentSnapshot.getString("address");
                            String employeeCode = documentSnapshot.getString("employeeCode");
                            String birthDate = documentSnapshot.getString("birthDate");

                            if (editPhone != null && phone != null) editPhone.setText(phone);
                            if (editAddress != null && address != null)
                                editAddress.setText(address);

                            if (editEmployeeCode != null) {
                                if (employeeCode != null && !employeeCode.isEmpty()) {
                                    editEmployeeCode.setText(employeeCode);
                                } else {
                                    editEmployeeCode.setText("Chưa có mã");
                                }
                            }

                            if (editBirthDate != null && birthDate != null)
                                editBirthDate.setText(birthDate);
                        }
                    });

            // Sử dụng repo để lấy Tên hiển thị cũ
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

        // 4. Sự kiện bấm nút LƯU -> Cập nhật dữ liệu
        if (btnSaveProfile != null) {
            btnSaveProfile.setOnClickListener(v -> {
                if (firebaseUser == null) return;

                String updatedName = editName != null ? editName.getText().toString().trim() : "";
                String updatedPhone = editPhone != null ? editPhone.getText().toString().trim() : "";
                String updatedAddress = editAddress != null ? editAddress.getText().toString().trim() : "";
                String updatedBirthDate = editBirthDate != null ? editBirthDate.getText().toString().trim() : "";

                if (updatedName.isEmpty()) {
                    Toast.makeText(this, "Họ tên không được trống!", Toast.LENGTH_SHORT).show();
                    return;
                }

                Map<String, Object> data = new HashMap<>();
                data.put("phone", updatedPhone);
                data.put("address", updatedAddress);
                data.put("birthDate", updatedBirthDate);

                FirebaseFirestore.getInstance().collection("users")
                        .document(firebaseUser.getUid())
                        .set(data, com.google.firebase.firestore.SetOptions.merge())
                        .addOnSuccessListener(unused -> {
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
                        })
                        .addOnFailureListener(e -> {
                            Toast.makeText(ProfileDetailActivity.this, "Lỗi lưu thông tin chi tiết!", Toast.LENGTH_SHORT).show();
                        });
            });
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_DETAIL_REQUEST
                && resultCode == RESULT_OK
                && data != null
                && data.getData() != null) {

            Uri selectedImageUri = data.getData();

            // Lưu ảnh vào bộ nhớ trong của ứng dụng
            saveAvatarToInternalStorage(selectedImageUri);
        }
    }

    private void saveAvatarToInternalStorage(Uri imageUri) {
        try {
            InputStream inputStream = getContentResolver().openInputStream(imageUri);

            if (inputStream == null) return;

            Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
            inputStream.close();

            File avatarFile = new File(getFilesDir(), "avatar.jpg");

            FileOutputStream outputStream = new FileOutputStream(avatarFile);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, outputStream);
            outputStream.flush();
            outputStream.close();

            if (detailAvatar != null) {
                detailAvatar.setImageBitmap(bitmap);
            }

            getSharedPreferences("USER_PROFILE", MODE_PRIVATE)
                    .edit()
                    .putString("avatar_path", avatarFile.getAbsolutePath())
                    .apply();

            Toast.makeText(this, "Đã lưu ảnh đại diện", Toast.LENGTH_SHORT).show();

        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Lỗi lưu ảnh", Toast.LENGTH_SHORT).show();
        }
    }

    private void loadSavedAvatar() {
        String avatarPath = getSharedPreferences("USER_PROFILE", MODE_PRIVATE)
                .getString("avatar_path", "");

        if (avatarPath.isEmpty()) return;

        File avatarFile = new File(avatarPath);

        if (avatarFile.exists()) {
            Bitmap bitmap = BitmapFactory.decodeFile(avatarFile.getAbsolutePath());

            if (detailAvatar != null) {
                detailAvatar.setImageBitmap(bitmap);
            }
        }
    }
}