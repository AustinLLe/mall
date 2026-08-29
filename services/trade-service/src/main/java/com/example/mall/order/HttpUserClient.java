package com.example.mall.order;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.server.ResponseStatusException;

@Component
public class HttpUserClient implements UserClient {
    private final RestClient client;

    public HttpUserClient(RestClient userRestClient) {
        this.client = userRestClient;
    }

    @Override
    public void requireActiveUserAndAddress(long userId, long addressId) {
        try {
            UserSnapshot user = client.get().uri("/internal/users/{id}", userId).retrieve().body(UserSnapshot.class);
            if (user == null || !"normal".equalsIgnoreCase(user.status())) {
                throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY, "user is not active");
            }
            client.get().uri("/internal/users/{userId}/addresses/{addressId}", userId, addressId)
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

    private record UserSnapshot(long userId, String status) {}
}
