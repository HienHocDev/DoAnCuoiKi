package com.example.doancuoiki.utils;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import androidx.core.app.NotificationCompat;

import com.example.doancuoiki.TaskDetailActivity;

public class NotificationReceiver extends BroadcastReceiver {
    
    private static final String CHANNEL_ID = "task_reminder_channel";
    
    @Override
    public void onReceive(Context context, Intent intent) {
        String taskId = intent.getStringExtra("TASK_ID");
        String title = intent.getStringExtra("TASK_TITLE");
        String desc = intent.getStringExtra("TASK_DESC");
        
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "Nhắc nhở công việc",
                    NotificationManager.IMPORTANCE_HIGH
            );
            channel.setDescription("Kênh thông báo nhắc nhở công việc sắp tới hạn");
            if (notificationManager != null) {
                notificationManager.createNotificationChannel(channel);
            }
        }
        
        Intent openIntent = new Intent(context, TaskDetailActivity.class);
        openIntent.putExtra(TaskDetailActivity.EXTRA_TASK_ID, taskId);
        openIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        
        PendingIntent pendingIntent = PendingIntent.getActivity(
                context,
                taskId != null ? taskId.hashCode() : 0,
                openIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );
        
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(android.R.drawable.ic_popup_reminder)
                .setContentTitle(title != null && !title.isEmpty() ? title : "Nhắc nhở công việc")
                .setContentText(desc != null && !desc.isEmpty() ? desc : "Bạn có một công việc sắp tới hạn")
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent);
                
        if (notificationManager != null) {
            notificationManager.notify(taskId != null ? taskId.hashCode() : (int) System.currentTimeMillis(), builder.build());
        }
    }
}
