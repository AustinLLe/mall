package com.example.shopping_back.auth.dto;

public class AuthUserView {

    private final Integer userId;
    private final String username;
    private final String phoneMasked;
    private final Integer credit;

    public AuthUserView(Integer userId, String username, String phoneMasked, Integer credit) {
        this.userId = userId;
        this.username = username;
        this.phoneMasked = phoneMasked;
        this.credit = credit;
    }

    public Integer getUserId() {
        return userId;
    }

    public String getUsername() {
        return username;
    }

    public String getPhoneMasked() {
        return phoneMasked;
    }

    public Integer getCredit() {
        return credit;
    }
}
