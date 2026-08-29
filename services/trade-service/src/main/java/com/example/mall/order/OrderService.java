package com.example.mall.order;

import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.Optional;

@Service
public class OrderService {
    private final JdbcClient jdbc;
    private final CatalogClient catalog;
    private final UserClient users;

    public OrderService(JdbcClient jdbc, CatalogClient catalog, UserClient users) {
        this.jdbc = jdbc;
        this.catalog = catalog;
        this.users = users;
    }

    public OrderView create(CreateOrder request) {
        Optional<OrderView> existing = findByRequestId(request.clientRequestId());
        if (existing.isPresent()) {
            return completeProductMark(existing.get());
        }

        users.requireActiveUserAndAddress(request.buyerId(), request.addressId());
        CatalogClient.ProductSnapshot product = catalog.getProduct(request.productId());
        if (!"ON_SALE".equals(product.status())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "product is not on sale");
        }

        jdbc.sql("""
                INSERT INTO orders(client_request_id, buyer_id, seller_id, product_id,
                    product_name, unit_price, quantity, amount, status)
                VALUES (:requestId, :buyerId, :sellerId, :productId, :name, :price, :quantity, :amount, 'PENDING_PRODUCT_MARK')
                """)
                .param("requestId", request.clientRequestId())
                .param("buyerId", request.buyerId())
                .param("sellerId", product.sellerId())
                .param("productId", product.productId())
                .param("name", product.name())
                .param("price", product.price())
                .param("quantity", request.quantity())
                .param("amount", product.price().multiply(BigDecimal.valueOf(request.quantity())))
                .update();
        return completeProductMark(findByRequestId(request.clientRequestId()).orElseThrow());
    }

    private OrderView completeProductMark(OrderView order) {
        if ("CONFIRMED".equals(order.status()) || "CANCELLED".equals(order.status())) return order;
        try {
            catalog.markSold(order.productId(), order.clientRequestId());
            jdbc.sql("UPDATE orders SET status='CONFIRMED' WHERE order_id=:id")
                    .param("id", order.orderId()).update();
            return find(order.orderId());
        } catch (ResponseStatusException exception) {
            jdbc.sql("""
                    UPDATE orders
                    SET compensation_attempts=compensation_attempts+1,
                        status=CASE WHEN compensation_attempts + 1 >= 3 THEN 'CANCELLED' ELSE 'COMPENSATION_REQUIRED' END
                    WHERE order_id=:id
                    """).param("id", order.orderId()).update();
            throw exception;
        }
    }

    public OrderView find(long id) {
        return jdbc.sql("""
                SELECT order_id, client_request_id, buyer_id, seller_id, product_id,
                    product_name, unit_price, quantity, amount, status
                FROM orders WHERE order_id=:id
                """).param("id", id).query(OrderView.class).optional()
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "order not found"));
    }

    private Optional<OrderView> findByRequestId(String requestId) {
        return jdbc.sql("""
                SELECT order_id, client_request_id, buyer_id, seller_id, product_id,
                    product_name, unit_price, quantity, amount, status
                FROM orders WHERE client_request_id=:requestId
                """).param("requestId", requestId).query(OrderView.class).optional();
    }

    public record CreateOrder(String clientRequestId, long buyerId, long addressId, long productId, int quantity) {}
    public record OrderView(long orderId, String clientRequestId, long buyerId, long sellerId, long productId,
                            String productName, BigDecimal unitPrice, int quantity, BigDecimal amount, String status) {}
}
