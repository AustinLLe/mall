package com.example.shopping_back.auth;

import com.example.shopping_back.auth.dto.AuthUserView;
import com.example.shopping_back.auth.dto.LoginRequest;
import com.example.shopping_back.auth.dto.LoginResponse;
import com.example.shopping_back.auth.dto.RegisterRequest;
import com.example.shopping_back.auth.model.StoredUser;
import com.example.shopping_back.auth.mapper.UserMapper;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 内存版用户与登录态，便于课程大作业先跑通前后端；后续可替换为 MySQL + JWT。
 */
@Service
public class AuthService {

    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
    private final UserMapper userMapper;
    private final Map<String, String> tokenToUsername = new ConcurrentHashMap<>();

    public AuthService(UserMapper userMapper) {
        this.userMapper = userMapper;
        if (this.userMapper.findByUsername("demo") == null) {
            StoredUser admin = new StoredUser(
                    "demo",
                    encoder.encode("demo123"),
                    "13800138000"
            );
            this.userMapper.insertUser(admin);
        }
    }

    public synchronized LoginResponse register(RegisterRequest req) {
        String u = req.getUsername().trim();
        if (userMapper.findByUsername(u) != null) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "用户名已存在");
        }
        String phone = req.getPhone() == null ? "" : req.getPhone().trim();
        StoredUser user = new StoredUser(u, encoder.encode(req.getPassword()), phone);
        userMapper.insertUser(user);
        return issueToken(user);
    }

    public LoginResponse login(LoginRequest req) {
        StoredUser user = userMapper.findByUsername(req.getUsername().trim());
        if (user == null || !encoder.matches(req.getPassword(), user.getPasswordHash())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "用户名或密码错误");
        }
        return issueToken(user);
    }

    public AuthUserView me(String token) {
        if (token == null || token.isBlank()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "未登录");
        }
        String username = tokenToUsername.get(token.trim());
        if (username == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "登录已失效");
        }
        StoredUser user = userMapper.findByUsername(username);
        if (user == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "用户不存在");
        }
        return toView(user);
    }

    private LoginResponse issueToken(StoredUser user) {
        String token = UUID.randomUUID().toString().replace("-", "");
        tokenToUsername.put(token, user.getUsername());
        return new LoginResponse(token, toView(user));
    }

    private static AuthUserView toView(StoredUser user) {
        String phone = user.getPhone();
        String masked = "";
        if (phone != null && phone.length() == 11) {
            masked = phone.substring(0, 3) + "****" + phone.substring(7);
        }
        return new AuthUserView(
            user.getUserId(),
            user.getUsername(), 
            masked, 
            user.getCredit() != null ? user.getCredit() : 100);
    }

    public List<AuthUserView> searchUsers(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return new java.util.ArrayList<>();
        }
        List<StoredUser> users = userMapper.searchUsersByKeyword(keyword.trim());
        List<AuthUserView> views = new java.util.ArrayList<>();
        for (StoredUser user : users) {
            String phone = user.getPhone();
            String masked = (phone != null && phone.length() == 11) ? phone.substring(0, 3) + "****" + phone.substring(7) : "";
            views.add(new AuthUserView(user.getUserId(), user.getUsername(), masked, user.getCredit()));
        }
        return views;
    }

}
