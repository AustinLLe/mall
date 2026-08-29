package com.example.mall.order;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.server.ResponseStatusException;

@Component
public class HttpCatalogClient implements CatalogClient {
    private final RestClient client;

    public HttpCatalogClient(RestClient catalogRestClient) {
        this.client = catalogRestClient;
    }

    @Override
    public ProductSnapshot getProduct(long productId) {
        try {
            ProductSnapshot product = client.get().uri("/internal/products/{id}/snapshot", productId)
                    .retrieve().body(ProductSnapshot.class);
            if (product == null) {
                throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "catalog returned an empty response");
            }
            return product;
        } catch (RestClientResponseException exception) {
            if (exception.getStatusCode().value() == 404) {
                throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY, "product does not exist");
            }
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "catalog request failed", exception);
        } catch (ResourceAccessException exception) {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "catalog is unavailable", exception);
        }
    }

    @Override
    public void markSold(long productId, String orderNumber) {
        try {
            client.put().uri("/internal/products/{id}/sold", productId)
                    .header("X-Order-Number", orderNumber).retrieve().toBodilessEntity();
        } catch (RestClientResponseException | ResourceAccessException exception) {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "catalog sale update failed", exception);
        }
    }
}
