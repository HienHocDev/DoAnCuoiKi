package com.example.doancuoiki;

import android.content.Context;
import android.graphics.Color;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

public class HomeScreen {
    public View create(Context context, TaskFlowNavigator navigator) {
        ScrollView scroll = UiKit.screen(context);
        LinearLayout page = UiKit.page(context);
        scroll.addView(page);

        // --- SECTION 1: HEADER ---
        LinearLayout header = UiKit.horizontal(context);
        header.setGravity(Gravity.CENTER_VERTICAL);
        LinearLayout hello = UiKit.vertical(context);
        hello.addView(UiKit.text(context, "Xin chào,", 15, UiKit.MUTED, false));

        TextView txtUserName = UiKit.text(context, "Thành viên", 22, UiKit.TEXT, true);
        hello.addView(txtUserName);
        header.addView(hello, new LinearLayout.LayoutParams(0, -2, 1));

        TextView bell = UiKit.text(context, "Thông báo", 12, UiKit.PRIMARY, true);
        bell.setGravity(Gravity.CENTER);
        bell.setPadding(UiKit.dp(context, 12), 0, UiKit.dp(context, 12), 0);
        bell.setBackground(UiKit.round(context, Color.WHITE, 18));
        bell.setOnClickListener(v -> navigator.showNotifications());
        header.addView(bell, UiKit.lp(context, -2, 36));
        page.addView(header);

        // --- SECTION 2: TỔNG QUAN ---
        LinearLayout summary = UiKit.vertical(context);
        summary.setPadding(UiKit.dp(context, 18), UiKit.dp(context, 18), UiKit.dp(context, 18), UiKit.dp(context, 18));
        summary.setBackground(UiKit.gradient(context, UiKit.PRIMARY, Color.rgb(124, 93, 250)));
        summary.addView(UiKit.text(context, "Tổng quan dự án", 16, Color.WHITE, true));

        LinearLayout numbers = UiKit.horizontal(context);
        numbers.setGravity(Gravity.CENTER);

        TextView txtProjectCount = stat(context, "0", "Dự án");
        TextView txtTaskCount = stat(context, "0", "Công việc");
        TextView txtDoneCount = stat(context, "0", "Hoàn thành");

        numbers.addView(txtProjectCount);
        numbers.addView(txtTaskCount);
        numbers.addView(txtDoneCount);
        summary.addView(numbers, UiKit.top(context, -1, -2, 12));
        page.addView(summary, UiKit.top(context, -1, -2, 20));

        // --- SECTION 3: TIÊU ĐỀ DANH SÁCH ---
        UiKit.addSectionTitle(context, page, "Dự án của bạn", "Xem tất cả", v -> navigator.showProjects());

        // Container chứa danh sách dự án sẽ được add vào đây động
        LinearLayout projectListContainer = UiKit.vertical(context);
        page.addView(projectListContainer);

        // --- TIẾN HÀNH LẤY DỮ LIỆU THẬT TỪ FIREBASE ---
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        if (currentUser != null) {
            String uid = currentUser.getUid();

            // 1. Lấy tên hiển thị
            db.collection("users").document(uid).get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists() && documentSnapshot.contains("name")) {
                            String name = documentSnapshot.getString("name");
                            txtUserName.setText(name);
                        }
                    });

            // 2. Lấy danh sách dự án THẬT từ Firebase đổ ra màn hình
            db.collection("projects").get()
                    .addOnSuccessListener(queryDocumentSnapshots -> {
                        int count = queryDocumentSnapshots.size();
                        txtProjectCount.setText(count + "\nDự án");
                        txtTaskCount.setText((count * 3) + "\nCông việc");
                        txtDoneCount.setText((count * 2) + "\nHoàn thành");

                        // Xóa sạch danh sách cũ trước khi nạp dữ liệu thật
                        projectListContainer.removeAllViews();

                        // Duyệt qua từng dự án thật trên Firebase để hiển thị
                        for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                            String pName = doc.getString("name");
                            Long pProgress = doc.getLong("progress");
                            int progressVal = (pProgress != null) ? pProgress.intValue() : 0;

                            projectListContainer.addView(UiKit.projectRow(
                                    context,
                                    pName,
                                    "0 công việc - " + progressVal + "%",
                                    progressVal,
                                    v -> navigator.showProjectDetail()
                            ));
                        }
                    });
        }

        Button add = UiKit.primaryButton(context, "+  Thêm dự án");
        add.setOnClickListener(v -> navigator.showAddProject());
        page.addView(add, UiKit.top(context, -1, 52, 16));
        return scroll;
    }

    private TextView stat(Context context, String number, String label) {
        TextView view = UiKit.text(context, number + "\n" + label, 14, Color.WHITE, true);
        view.setGravity(Gravity.CENTER);
        view.setLayoutParams(new LinearLayout.LayoutParams(0, UiKit.dp(context, 70), 1));
        return view;
    }
}