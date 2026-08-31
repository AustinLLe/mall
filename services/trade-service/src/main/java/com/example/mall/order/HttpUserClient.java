package com.example.mall.order;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.server.ResponseStatusException;

@Component
public class HttpUserClient implements UserClient {
    private final RestClient authClient;
    private final RestClient userClient;

    public HttpUserClient(RestClient authRestClient, RestClient userRestClient) {
        this.authClient = authRestClient;
        this.userClient = userRestClient;
    }

    @Override
    public CurrentUser requireLogin(String authorization) {
        if (authorization == null || authorization.isBlank()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "请先登录");
        }
        try {
            JsonNode body = authClient.get().uri("/api/auth/me")
                    .header("Authorization", authorization)
                    .retrieve()
                    .body(JsonNode.class);
            JsonNode data = body == null ? null : body.path("data");
            if (data == null || data.isMissingNode() || data.isNull() || !data.hasNonNull("userId")) {
                throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "请先登录");
            }
            CurrentUser user = new CurrentUser(data.get("userId").asLong(), text(data, "status"));
            if (!user.isActive()) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "账号不可用");
            }
            return user;
        } catch (ResponseStatusException exception) {
            throw exception;
        } catch (RestClientResponseException exception) {
            int status = exception.getStatusCode().value();
            if (status == 401 || status == 403) {
                throw new ResponseStatusException(HttpStatus.valueOf(status), "请先登录");
            }
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "登录服务暂时不可用", exception);
        } catch (ResourceAccessException exception) {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "登录服务暂时不可用", exception);
        }
    }

    @Override
    public void requireActiveUser(long userId) {
        try {
            UserSnapshot user = userClient.get().uri("/internal/users/{id}", userId).retrieve().body(UserSnapshot.class);
            if (user == null || !"normal".equalsIgnoreCase(user.status())) {
                throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY, "user is not active");
            }
        } catch (ResponseStatusException exception) {
            throw exception;
        } catch (RestClientResponseException exception) {
            if (exception.getStatusCode().value() == 404) {
                throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY, "user or address does not exist");
            }
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "user service request failed", exception);
        } catch (ResourceAccessException exception) {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "user service is unavailable", exception);
        }
    }

    @Override
    public void requireActiveUserAndAddress(long userId, long addressId) {
        requireActiveUser(userId);
        try {
            userClient.get().uri("/internal/users/{userId}/addresses/{addressId}", userId, addressId)
                    .retrieve().toBodilessEntity();
        } catch (RestClientResponseException exception) {
            if (exception.getStatusCode().value() == 404) {
                throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY, "user or address does not exist");
            }
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "user service request failed", exception);
        } catch (ResourceAccessException exception) {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "user service is unavailable", exception);
        }
    }

    private static String text(JsonNode data, String field) {
        JsonNode node = data.path(field);
        return node.isMissingNode() || node.isNull() ? "" : node.asText();
    }

    private record UserSnapshot(long userId, String status) {}
}
