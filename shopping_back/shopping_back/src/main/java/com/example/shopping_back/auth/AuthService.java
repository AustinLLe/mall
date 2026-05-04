package com.example.shopping_back.auth;

import com.example.shopping_back.auth.dto.AuthUserView;
import com.example.shopping_back.auth.dto.LoginRequest;
import com.example.shopping_back.auth.dto.LoginResponse;
import com.example.shopping_back.auth.dto.RegisterRequest;
import com.example.shopping_back.auth.model.StoredUser;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 内存版用户与登录态，便于课程大作业先跑通前后端；后续可替换为 MySQL + JWT。
 */
@Service
public class AuthService {

    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
    private final Map<String, StoredUser> usersByUsername = new ConcurrentHashMap<>();
    private final Map<String, String> tokenToUsername = new ConcurrentHashMap<>();

    public AuthService() {
        StoredUser admin = new StoredUser(
                "demo",
                encoder.encode("demo123"),
                "演示用户",
                "13800138000"
        );
        usersByUsername.put(admin.getUsername(), admin);
    }

    public synchronized LoginResponse register(RegisterRequest req) {
        String u = req.getUsername().trim();
        if (usersByUsername.containsKey(u)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "用户名已存在");
        }
        String phone = req.getPhone() == null ? "" : req.getPhone().trim();
        String nickname = u.length() > 8 ? u.substring(0, 8) : u;
        StoredUser user = new StoredUser(u, encoder.encode(req.getPassword()), nickname, phone);
        usersByUsername.put(u, user);
        return issueToken(user);
    }

    public LoginResponse login(LoginRequest req) {
        StoredUser user = usersByUsername.get(req.getUsername().trim());
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
        StoredUser user = usersByUsername.get(username);
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
        return new AuthUserView(user.getUsername(), user.getNickname(), masked, 100);
    }
}
