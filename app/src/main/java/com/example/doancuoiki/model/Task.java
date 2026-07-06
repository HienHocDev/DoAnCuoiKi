package com.example.doancuoiki.model;

public class Task {
    public static final String STATUS_NOT_STARTED = "Chưa bắt đầu";
    public static final String STATUS_IN_PROGRESS = "Đang làm";
    public static final String STATUS_DONE = "Hoàn thành";
    public static final String STATUS_PENDING = "Đang chờ";
    public static final String STATUS_CANCELLED = "Đã hủy";

    private String id;
    private String projectId;
    private String projectName;
    private String title;
    private String description;
    private String assigneeId;
    private String assigneeName;
    private String creatorId;
    private String status;
    private String priority;
    private String startDate;
    private String dueDate;
    private String category;
    private String reminderTime;
    private String reminderType;

    public Task() {
    }

    public Task(String id, String projectId, String projectName, String title, String description,
                String assigneeId, String assigneeName, String creatorId, String status,
                String priority, String startDate, String dueDate) {
        this.id = id;
        this.projectId = projectId;
        this.projectName = projectName;
        this.title = title;
        this.description = description;
        this.assigneeId = assigneeId;
        this.assigneeName = assigneeName;
        this.creatorId = creatorId;
        this.status = status;
        this.priority = priority;
        this.startDate = startDate;
        this.dueDate = dueDate;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getProjectId() {
        return projectId;
    }

    public void setProjectId(String projectId) {
        this.projectId = projectId;
    }

    public String getProjectName() {
        return projectName;
    }

    public void setProjectName(String projectName) {
        this.projectName = projectName;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getAssigneeId() {
        return assigneeId;
    }

    public void setAssigneeId(String assigneeId) {
        this.assigneeId = assigneeId;
    }

    public String getAssigneeName() {
        return assigneeName;
    }

    public void setAssigneeName(String assigneeName) {
        this.assigneeName = assigneeName;
    }

    public String getCreatorId() {
        return creatorId;
    }

    public void setCreatorId(String creatorId) {
        this.creatorId = creatorId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getPriority() {
        return priority;
    }

    public void setPriority(String priority) {
        this.priority = priority;
    }

    public String getStartDate() {
        return startDate;
    }

    public void setStartDate(String startDate) {
        this.startDate = startDate;
    }

    public String getDueDate() {
        return dueDate;
    }

    public void setDueDate(String dueDate) {
        this.dueDate = dueDate;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getReminderTime() {
        return reminderTime;
    }

    public void setReminderTime(String reminderTime) {
        this.reminderTime = reminderTime;
    }

    public String getReminderType() {
        return reminderType;
    }

    public void setReminderType(String reminderType) {
        this.reminderType = reminderType;
    }
}
