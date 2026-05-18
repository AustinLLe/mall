package com.example.shopping_back.auth.model;

public class StoredUser {
    private Integer userId;
    private String username;
    private String passwordHash;
    private String phone;
    private Integer credit;

    public StoredUser() {
    }

    public StoredUser(String username, String passwordHash, String phone) {
        this.username = username;
        this.passwordHash = passwordHash;
        this.phone = phone;
    }

    public Integer getUserId() {
        return userId;
    }
    public void setUserId(Integer userId) {
        this.userId = userId;
    }
    public String getUsername() {
        return username;
    }
    public void setUsername(String username) {
        this.username = username;
    }
    public String getPasswordHash() {
        return passwordHash;
    }
    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }
    public String getPhone() {
        return phone;
    }
    public void setPhone(String phone) {
        this.phone = phone;
    }
    public Integer getCredit() {
        return credit;
    }
    public void setCredit(Integer credit) {
        this.credit = credit;
    }
}
