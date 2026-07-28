package com.example.shopping_back.auth;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.example.shopping_back.auth.dto.LoginRequest;
import com.example.shopping_back.auth.dto.LoginResponse;
import com.example.shopping_back.auth.dto.RegisterRequest;
import com.example.shopping_back.auth.mapper.UserMapper;
import com.example.shopping_back.auth.model.StoredUser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.server.ResponseStatusException;

class AuthServiceTest {

    private UserMapper userMapper;
    private AuthService authService;

    @BeforeEach
    void setUp() {
        userMapper = mock(UserMapper.class);
        when(userMapper.countUserColumn(anyString())).thenReturn(1);
        authService = new AuthService(userMapper);
    }

    @Test
    void loginReturnsTokenAndMasksPhone() {
        StoredUser user = user(
                "alice",
                new BCryptPasswordEncoder().encode("secret123"),
                "13800138000",
                "buyer",
                "normal");
        when(userMapper.findByUsername("alice")).thenReturn(user);

        LoginResponse response = authService.login(login("alice", "secret123"));

        assertNotNull(response.getToken());
        assertFalse(response.getToken().isBlank());
        assertEquals("alice", response.getUser().getUsername());
        assertEquals("138****8000", response.getUser().getPhoneMasked());
        assertEquals("buyer", response.getUser().getRole());
    }

    @Test
    void loginRejectsWrongPassword() {
        StoredUser user = user(
                "alice",
                new BCryptPasswordEncoder().encode("secret123"),
                "13800138000",
                "buyer",
                "normal");
        when(userMapper.findByUsername("alice")).thenReturn(user);

        ResponseStatusException error = assertThrows(
                ResponseStatusException.class,
                () -> authService.login(login("alice", "wrong-password")));

        assertEquals(HttpStatus.UNAUTHORIZED, error.getStatusCode());
    }

    @Test
    void loginRejectsDisabledAccount() {
        StoredUser user = user(
                "alice",
                new BCryptPasswordEncoder().encode("secret123"),
                "13800138000",
                "buyer",
                "disabled");
        when(userMapper.findByUsername("alice")).thenReturn(user);

        ResponseStatusException error = assertThrows(
                ResponseStatusException.class,
                () -> authService.login(login("alice", "secret123")));

        assertEquals(HttpStatus.FORBIDDEN, error.getStatusCode());
    }

    @Test
    void registerRejectsAdminRole() {
        RegisterRequest request = new RegisterRequest();
        request.setUsername("new-admin");
        request.setPassword("secret123");
        request.setPhone("13900139000");
        request.setRole("admin");

        ResponseStatusException error = assertThrows(
                ResponseStatusException.class,
                () -> authService.register(request));

        assertEquals(HttpStatus.BAD_REQUEST, error.getStatusCode());
    }

    private static LoginRequest login(String username, String password) {
        LoginRequest request = new LoginRequest();
        request.setUsername(username);
        request.setPassword(password);
        return request;
    }

    private static StoredUser user(
            String username,
            String passwordHash,
            String phone,
            String role,
            String status) {
        StoredUser user = new StoredUser(username, passwordHash, phone, role);
        user.setUserId(101);
        user.setCredit(100);
        user.setStatus(status);
        return user;
    }
}
