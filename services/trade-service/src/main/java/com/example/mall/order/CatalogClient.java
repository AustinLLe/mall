package com.example.mall.order;

import java.math.BigDecimal;

public interface CatalogClient {
    ProductSnapshot getProduct(long productId);
    void markSold(long productId, String orderNumber);

    record ProductSnapshot(long productId, long sellerId, String name, BigDecimal price, String status) {}
}
