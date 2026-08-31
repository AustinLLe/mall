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
import java.util.List;

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
        catalog.sellerId = 9;
        users.available = true;
        users.userId = 7;
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

    @Test
    void frontendCreateUsesTokenAndSnapshotWithoutJoin() {
        List<OrderService.StorefrontOrderView> orders = service.createFromItems(
                "Bearer test-token", List.of(new OrderService.OrderItem("42", 2)));

        OrderService.StorefrontOrderView created = orders.stream()
                .filter(order -> "42".equals(order.goodsId()))
                .findFirst()
                .orElseThrow();
        assertThat(created.title()).isEqualTo("接口返回的商品");
        assertThat(created.shop()).isEqualTo("接口店铺");
        assertThat(created.cover()).isEqualTo("/cover.png");
        assertThat(created.status()).isEqualTo("已完成");
        assertThat(created.reviewable()).isTrue();
        assertThat(created.amount()).isEqualByComparingTo("39.80");
        assertThat(created.goodsId()).isEqualTo("42");
    }

    @Test
    void frontendCreateRejectsBuyingOwnProduct() {
        catalog.sellerId = 7;
        assertThatThrownBy(() -> service.createFromItems(
                "Bearer test-token", List.of(new OrderService.OrderItem("42", 1))))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(error -> assertThat(((ResponseStatusException) error).getStatusCode())
                        .isEqualTo(HttpStatus.BAD_REQUEST));
    }

    @Test
    void cancelAndReviewUseStoredSnapshot() {
        service.createFromItems("Bearer test-token", List.of(new OrderService.OrderItem("88", 1)));
        OrderService.StorefrontOrderView listed = service.listStorefront("Bearer test-token", null).stream()
                .filter(order -> "88".equals(order.goodsId()))
                .findFirst()
                .orElseThrow();

        OrderService.StorefrontOrderView reviewed = service.review(
                "Bearer test-token", listed.id(), new OrderService.ReviewRequest(5, 4, "很好"));
        assertThat(reviewed.reviewed()).isTrue();
        assertThat(reviewed.status()).isEqualTo("已评价");
        assertThat(reviewed.title()).isEqualTo("接口返回的商品");

        assertThatThrownBy(() -> service.review(
                "Bearer test-token", listed.id(), new OrderService.ReviewRequest(5, 4, "重复")))
                .isInstanceOf(ResponseStatusException.class);
    }

    @Test
    void cancelOwnOrder() {
        service.createFromItems("Bearer test-token", List.of(new OrderService.OrderItem("91", 1)));
        String id = service.listStorefront("Bearer test-token", null).stream()
                .filter(order -> "91".equals(order.goodsId()))
                .findFirst()
                .orElseThrow()
                .id();
        assertThat(service.cancel("Bearer test-token", id).status()).isEqualTo("已取消");
    }

    @Test
    void missingTokenIsUnauthorized() {
        assertThatThrownBy(() -> service.listStorefront(null, null))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(error -> assertThat(((ResponseStatusException) error).getStatusCode())
                        .isEqualTo(HttpStatus.UNAUTHORIZED));
    }

    @TestConfiguration
    static class StubConfiguration {
        @Bean @Primary StubCatalogClient stubCatalogClient() { return new StubCatalogClient(); }
        @Bean @Primary StubUserClient stubUserClient() { return new StubUserClient(); }
    }

    static class StubCatalogClient implements CatalogClient {
        boolean productAvailable = true;
        boolean markSoldAvailable = true;
        long sellerId = 9;
        @Override public ProductSnapshot getProduct(long productId) {
            if (!productAvailable) throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "catalog unavailable");
            return new ProductSnapshot(productId, sellerId, "接口返回的商品", new BigDecimal("19.90"), "ON_SALE",
                    "接口店铺", "/cover.png", "used");
        }
        @Override public void markSold(long productId, String orderNumber) {
            if (!markSoldAvailable) throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "catalog unavailable");
        }
    }

    static class StubUserClient implements UserClient {
        boolean available = true;
        long userId = 7;
        @Override public CurrentUser requireLogin(String authorization) {
            if (!available) throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "user unavailable");
            if (authorization == null || authorization.isBlank()) {
                throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "请先登录");
            }
            return new CurrentUser(userId, "normal");
        }
        @Override public void requireActiveUser(long ignoredUserId) {
            if (!available) throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "user unavailable");
        }
        @Override public void requireActiveUserAndAddress(long userId, long addressId) {
            requireActiveUser(userId);
        }
    }
}
