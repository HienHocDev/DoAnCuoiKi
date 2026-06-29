package com.example.doancuoiki.model;

import com.google.firebase.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class ActivityLog {
    private String projectId;
    private String userName;
    private String actionText;
    private String targetName;
    private String type;
    private Timestamp timestamp;

    public ActivityLog() {
    }

    public String getProjectId() { return projectId; }
    public String getUserName() { return userName; }
    public String getActionText() { return actionText; }
    public String getTargetName() { return targetName; }
    public String getType() { return type; }
    public Timestamp getTimestamp() { return timestamp; }
    public void setProjectId(String projectId) { this.projectId = projectId; }
    public void setUserName(String userName) { this.userName = userName; }
    public void setActionText(String actionText) { this.actionText = actionText; }
    public void setTargetName(String targetName) { this.targetName = targetName; }
    public void setType(String type) { this.type = type; }
    public void setTimestamp(Timestamp timestamp) { this.timestamp = timestamp; }
    public boolean isComment() {
        return "comment".equals(type);
    }

    public String getTimeAgo() {
        if (timestamp == null) return "Vừa xong";
        long diff = new Date().getTime() - timestamp.toDate().getTime();
        long seconds = diff / 1000;
        long minutes = seconds / 60;
        long hours = minutes / 60;
        long days = hours / 24;

        if (days > 0) return days + " ngày trước";
        if (hours > 0) return hours + " giờ trước";
        if (minutes > 0) return minutes + " phút trước";
        return "Vừa xong";
    }
}