package com.example.doancuoiki.utils;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import com.example.doancuoiki.model.Task;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class AlarmUtils {
    
    public static void scheduleTaskAlarm(Context context, Task task) {
        if (task == null || task.getId() == null) return;
        
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(context, NotificationReceiver.class);
        intent.putExtra("TASK_ID", task.getId());
        intent.putExtra("TASK_TITLE", task.getTitle());
        intent.putExtra("TASK_DESC", task.getDescription());
        
        int requestCode = task.getId().hashCode();
        
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context, 
                requestCode, 
                intent, 
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        String type = task.getReminderType();
        if (type == null || type.equals("Không nhắc") || task.getDueDate() == null || task.getReminderTime() == null || task.getReminderTime().isEmpty() || task.getDueDate().equals("Chưa có hạn")) {
            if (alarmManager != null) {
                alarmManager.cancel(pendingIntent);
            }
            return;
        }

        try {
            String dateTimeString = task.getDueDate() + " " + task.getReminderTime();
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
            Date date = sdf.parse(dateTimeString);
            
            if (date != null) {
                Calendar calendar = Calendar.getInstance();
                calendar.setTime(date);
                
                if (type.equals("1 giờ")) {
                    calendar.add(Calendar.HOUR_OF_DAY, -1);
                } else if (type.equals("1 ngày")) {
                    calendar.add(Calendar.DAY_OF_YEAR, -1);
                } else if (type.equals("3 ngày")) {
                    calendar.add(Calendar.DAY_OF_YEAR, -3);
                }
                
                long alarmTime = calendar.getTimeInMillis();
                
                if (alarmTime > System.currentTimeMillis()) {
                    if (alarmManager != null) {
                        try {
                            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, alarmTime, pendingIntent);
                        } catch (SecurityException e) {
                            alarmManager.set(AlarmManager.RTC_WAKEUP, alarmTime, pendingIntent);
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
