package com.example.doancuoiki.repository;

import com.example.doancuoiki.model.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class AuthRepository {
    public interface AuthCallback {
        void onSuccess(FirebaseUser firebaseUser);

        void onError(Exception exception);
    }

    public interface SimpleCallback {
        void onSuccess();

        void onError(Exception exception);
    }

    private final FirebaseAuth auth = FirebaseAuth.getInstance();
    private final UserRepository userRepository = new UserRepository();

    public FirebaseUser getCurrentUser() {
        return auth.getCurrentUser();
    }

    public void login(String email, String password, AuthCallback callback) {
        auth.signInWithEmailAndPassword(email, password)
                .addOnSuccessListener(result -> callback.onSuccess(result.getUser()))
                .addOnFailureListener(callback::onError);
    }

    // ==============================================================================
    // HÀM XỬ LÝ TRA CỨU MÃ NHÂN VIÊN CHUẨN XÁC VỚI FIRESTORE
    // ==============================================================================
    public void loginWithEmployeeCode(String code, String password, AuthCallback callback) {
        com.google.firebase.firestore.FirebaseFirestore.getInstance().collection("users") // Đổi sang chữ "users" thường đồng bộ với UserRepository
                .whereEqualTo("employeeCode", code)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (queryDocumentSnapshots != null && !queryDocumentSnapshots.isEmpty() && queryDocumentSnapshots.getDocuments().size() > 0) {
                        // Tìm thấy tài khoản có mã nhân viên trùng khớp
                        com.google.firebase.firestore.DocumentSnapshot document = queryDocumentSnapshots.getDocuments().get(0);
                        String realEmail = document.getString("email");

                        if (realEmail != null && !realEmail.trim().isEmpty()) {
                            // Thực hiện đăng nhập bằng Email thật vừa tìm được
                            login(realEmail, password, callback);
                        } else {
                            callback.onError(new Exception("Không tìm thấy email liên kết!"));
                        }
                    } else {
                        // Trả về Exception chuẩn để LoginActivity nhận diện đưa vào luồng onError
                        callback.onError(new Exception("Mã nhân viên không tồn tại!"));
                    }
                })
                .addOnFailureListener(callback::onError);
    }

    public void register(String name, String email, String password, AuthCallback callback) {
        auth.createUserWithEmailAndPassword(email, password)
                .addOnSuccessListener(result -> {
                    FirebaseUser firebaseUser = result.getUser();
                    if (firebaseUser == null) {
                        callback.onError(new IllegalStateException("Không tạo được tài khoản"));
                        return;
                    }

                    // Tự động sinh mã nhân viên ngẫu nhiên
                    String employeeCode = generateEmployeeCode();

                    // Khởi tạo đối tượng User với tham số Mã nhân viên ở cuối
                    User user = new User(
                            firebaseUser.getUid(),
                            name,
                            email,
                            "Thành viên",
                            "",
                            now(),
                            employeeCode
                    );

                    userRepository.createUser(user, new UserRepository.SimpleCallback() {
                        @Override
                        public void onSuccess() {
                            callback.onSuccess(firebaseUser);
                        }

                        @Override
                        public void onError(Exception exception) {
                            callback.onError(exception);
                        }
                    });
                })
                .addOnFailureListener(callback::onError);
    }

    public void sendPasswordReset(String email, SimpleCallback callback) {
        auth.sendPasswordResetEmail(email)
                .addOnSuccessListener(unused -> callback.onSuccess())
                .addOnFailureListener(callback::onError);
    }

    public void logout() {
        auth.signOut();
    }

    private String now() {
        return new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(new Date());
    }

    private String generateEmployeeCode() {
        int randomNumber = (int) (Math.random() * 9000) + 1000;
        return "TF" + randomNumber;
    }

    public void changePasswordDirectly(String currentPassword, String newPassword, SimpleCallback callback) {
        FirebaseUser user = auth.getCurrentUser();
        if (user == null || user.getEmail() == null) {
            callback.onError(new IllegalStateException("Người dùng chưa đăng nhập"));
            return;
        }

        com.google.firebase.auth.AuthCredential credential =
                com.google.firebase.auth.EmailAuthProvider.getCredential(user.getEmail(), currentPassword);

        user.reauthenticate(credential)
                .addOnSuccessListener(unused -> {
                    user.updatePassword(newPassword)
                            .addOnSuccessListener(unused2 -> callback.onSuccess())
                            .addOnFailureListener(callback::onError);
                })
                .addOnFailureListener(callback::onError);
    }
}