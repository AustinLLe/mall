package com.example.mall.catalog;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

public record CatalogUser(long userId, String role, String username) {
    public static CatalogUser required(Long userId, String role, String username) {
        if (userId == null || userId <= 0) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "authenticated user is required");
        }
        return new CatalogUser(userId, role == null ? "" : role.trim().toLowerCase(),
                username == null ? "" : username.trim());
    }

    public boolean isSeller() { return "seller".equals(role); }
}
