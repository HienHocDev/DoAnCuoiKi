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

    public void register(String name, String email, String password, AuthCallback callback) {
        auth.createUserWithEmailAndPassword(email, password)
                .addOnSuccessListener(result -> {
                    FirebaseUser firebaseUser = result.getUser();
                    if (firebaseUser == null) {
                        callback.onError(new IllegalStateException("Không tạo được tài khoản"));
                        return;
                    }

                    User user = new User(
                            firebaseUser.getUid(),
                            name,
                            email,
                            "Thành viên",
                            "",
                            now()
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
}
