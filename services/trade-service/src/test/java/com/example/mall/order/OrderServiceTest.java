package com.example.mall.order;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:trade;MODE=MySQL;DB_CLOSE_DELAY=-1",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.datasource.username=sa", "spring.datasource.password="})
class OrderServiceTest {
    @Autowired OrderService service;
    @Autowired StubCatalogClient catalog;
    @Autowired StubUserClient users;

    @BeforeEach
    void resetClients() {
        catalog.productAvailable = true;
        catalog.markSoldAvailable = true;
        users.available = true;
    }

    @Test
    void copiesProductSnapshotViaCatalogApiAndIsIdempotent() {
        OrderService.CreateOrder request = new OrderService.CreateOrder("test-request-1", 7, 3, 42, 2);
        OrderService.OrderView first = service.create(request);
        OrderService.OrderView repeated = service.create(request);

        assertThat(first.amount()).isEqualByComparingTo("39.80");
        assertThat(first.productName()).isEqualTo("接口返回的商品");
        assertThat(repeated.orderId()).isEqualTo(first.orderId());
    }

    @Test
    void catalogFailureDoesNotCreateAnOrder() {
        catalog.productAvailable = false;
        assertThatThrownBy(() -> service.create(new OrderService.CreateOrder("failed-request", 7, 3, 42, 1)))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(error -> assertThat(((ResponseStatusException) error).getStatusCode())
                        .isEqualTo(HttpStatus.SERVICE_UNAVAILABLE));
        assertThatThrownBy(() -> service.find(9999)).isInstanceOf(ResponseStatusException.class);
    }

    @Test
    void retriesProductMarkWithTheSameBusinessNumber() {
        OrderService.CreateOrder request = new OrderService.CreateOrder("compensation-request", 7, 3, 42, 1);
        catalog.markSoldAvailable = false;
        assertThatThrownBy(() -> service.create(request)).isInstanceOf(ResponseStatusException.class);

        catalog.markSoldAvailable = true;
        assertThat(service.create(request).status()).isEqualTo("CONFIRMED");
    }

    @Test
    void userServiceFailureStopsBeforeOrderInsert() {
        users.available = false;
        assertThatThrownBy(() -> service.create(
                new OrderService.CreateOrder("user-failed", 7, 3, 42, 1)))
                .isInstanceOf(ResponseStatusException.class);
    }

    @TestConfiguration
    static class StubConfiguration {
        @Bean @Primary StubCatalogClient stubCatalogClient() { return new StubCatalogClient(); }
        @Bean @Primary StubUserClient stubUserClient() { return new StubUserClient(); }
    }

    static class StubCatalogClient implements CatalogClient {
        boolean productAvailable = true;
        boolean markSoldAvailable = true;
        @Override public ProductSnapshot getProduct(long productId) {
            if (!productAvailable) throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "catalog unavailable");
            return new ProductSnapshot(productId, 9, "接口返回的商品", new BigDecimal("19.90"), "ON_SALE");
        }
        @Override public void markSold(long productId, String orderNumber) {
            if (!markSoldAvailable) throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "catalog unavailable");
        }
    }

    static class StubUserClient implements UserClient {
        boolean available = true;
        @Override public void requireActiveUserAndAddress(long userId, long addressId) {
            if (!available) throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "user unavailable");
        }
    }
}
