package com.example.shopping_back.auth.dto;

public class AuthUserView {

    private final String username;
    private final String nickname;
    private final String phoneMasked;
    private final int creditScore;

    public AuthUserView(String username, String nickname, String phoneMasked, int creditScore) {
        this.username = username;
        this.nickname = nickname;
        this.phoneMasked = phoneMasked;
        this.creditScore = creditScore;
    }

    public String getUsername() {
        return username;
    }

    public String getNickname() {
        return nickname;
    }

    public String getPhoneMasked() {
        return phoneMasked;
    }

    public int getCreditScore() {
        return creditScore;
    }
}
