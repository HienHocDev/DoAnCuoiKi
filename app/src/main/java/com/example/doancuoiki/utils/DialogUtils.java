package com.example.doancuoiki.utils;

import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.example.doancuoiki.R;

public class DialogUtils {

    /**
     * Hiển thị một dialog thông báo thành công ở giữa màn hình.
     * Sử dụng Custom Toast để tránh lỗi Crash (WindowLeaked) khi Activity bị đóng ngay sau đó.
     * @param context Context của Activity hiện tại
     * @param message Lời nhắn bạn muốn hiển thị
     */
    public static void showSuccessNotification(Context context, String message) {
        try {
            Toast toast = new Toast(context);
            toast.setDuration(Toast.LENGTH_SHORT);
            
            LayoutInflater inflater = LayoutInflater.from(context);
            View view = inflater.inflate(R.layout.dialog_success_notification, null);
            
            TextView tvMessage = view.findViewById(R.id.tvSuccessMessage);
            if (tvMessage != null) {
                tvMessage.setText(message);
            }
            
            toast.setView(view);
            toast.setGravity(Gravity.CENTER, 0, 0);
            toast.show();
        } catch (Exception e) {
            // Nếu có lỗi (ví dụ API quá cũ/mới không hỗ trợ Custom Toast), fallback về Toast mặc định
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
        }
    }
}
