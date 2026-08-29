package com.example.mall.catalog;

import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.List;

@Service
public class ProductService {
    private final JdbcClient jdbc;

    public ProductService(JdbcClient jdbc) {
        this.jdbc = jdbc;
    }

    public ProductView create(CreateProduct request) {
        jdbc.sql("INSERT INTO goods(seller_id, name, price, status) VALUES (:sellerId, :name, :price, 'ON_SALE')")
                .param("sellerId", request.sellerId()).param("name", request.name()).param("price", request.price()).update();
        return jdbc.sql("SELECT goods_id AS product_id, seller_id, name, price, status FROM goods ORDER BY goods_id DESC LIMIT 1")
                .query(ProductView.class).single();
    }

    public ProductView find(long id) {
        return jdbc.sql("SELECT goods_id AS product_id, seller_id, name, price, status FROM goods WHERE goods_id=:id")
                .param("id", id).query(ProductView.class).optional()
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "product not found"));
    }

    public List<ProductView> list() {
        return jdbc.sql("SELECT goods_id AS product_id, seller_id, name, price, status FROM goods ORDER BY goods_id")
                .query(ProductView.class).list();
    }

    public void markSold(long id, String orderNumber) {
        int changed = jdbc.sql("""
                UPDATE goods SET status='SOLD', sold_order_number=:orderNumber
                WHERE goods_id=:id AND (status='ON_SALE' OR sold_order_number=:orderNumber)
                """).param("id", id).param("orderNumber", orderNumber).update();
        if (changed == 0) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "product is not available");
        }
    }

    public record CreateProduct(long sellerId, String name, BigDecimal price) {}
    public record ProductView(long productId, long sellerId, String name, BigDecimal price, String status) {}
}
