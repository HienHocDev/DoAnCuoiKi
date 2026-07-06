package com.example.doancuoiki.model;

public class User {
    private String id;
    private String name;
    private String email;
    private String role;
    private String avatarUrl;
    private Object createdAt;
    // 1. THÊM THUỘC TÍNH MÃ NHÂN VIÊN VÀO ĐÂY
    private String employeeCode;

    public User() {
    }

    // 2. CẬP NHẬT HÀM KHỞI TẠO ĐẦY ĐỦ THAM SỐ
    public User(String id, String name, String email, String role, String avatarUrl, Object createdAt, String employeeCode) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.role = role;
        this.avatarUrl = avatarUrl;
        this.createdAt = createdAt;
        this.employeeCode = employeeCode;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getAvatarUrl() {
        return avatarUrl;
    }

    public void setAvatarUrl(String avatarUrl) {
        this.avatarUrl = avatarUrl;
    }

    public Object getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Object createdAt) {
        this.createdAt = createdAt;
    }

    // 3. THÊM HÀM GETTER VÀ SETTER CHO MÃ NHÂN VIÊN
    public String getEmployeeCode() {
        return employeeCode;
    }

    public void setEmployeeCode(String employeeCode) {
        this.employeeCode = employeeCode;
    }
}