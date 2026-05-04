package com.example.shopping_back.auth.model;

public class StoredUser {

    private final String username;
    private final String passwordHash;
    private final String nickname;
    private final String phone;

    public StoredUser(String username, String passwordHash, String nickname, String phone) {
        this.username = username;
        this.passwordHash = passwordHash;
        this.nickname = nickname;
        this.phone = phone;
    }

    public String getUsername() {
        return username;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public String getNickname() {
        return nickname;
    }

    public String getPhone() {
        return phone;
    }
}
