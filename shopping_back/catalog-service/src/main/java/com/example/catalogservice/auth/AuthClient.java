package com.example.catalogservice.auth;

import com.example.common.dto.ApiResult;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ResponseStatusException;

/**
 * 通过 HTTP 调用 user-service 解析登录 Token。
 * 原 monolith 中 AuthService.me() 的替代实现（Token 存于 user-service 内存中）。
 */
@Component
public class AuthClient {

    private final RestTemplate restTemplate;
    private final String baseUrl;

    public AuthClient(RestTemplateBuilder builder,
                      @Value("${user-service.base-url:http://127.0.0.1:8081}") String baseUrl) {
        this.restTemplate = builder.build();
        this.baseUrl = baseUrl == null ? "" : baseUrl.replaceAll("/+$", "");
    }

    public AuthUserView me(String token) {
        if (token == null || token.isBlank()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Not logged in");
        }
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token.trim());
        try {
            ResponseEntity<ApiResult<AuthUserView>> resp = restTemplate.exchange(
                    baseUrl + "/api/auth/me",
                    HttpMethod.GET,
                    new HttpEntity<>(headers),
                    new ParameterizedTypeReference<ApiResult<AuthUserView>>() {});
            ApiResult<AuthUserView> body = resp.getBody();
            if (body == null || body.getData() == null) {
                throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Login expired");
            }
            return body.getData();
        } catch (HttpStatusCodeException e) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Login expired");
        }
    }
}
