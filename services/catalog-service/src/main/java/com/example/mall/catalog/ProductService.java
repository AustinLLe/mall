package com.example.mall.catalog;

import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class ProductService {
    static final String PRODUCT_COLUMNS = """
            SELECT goods_id AS product_id, seller_id, name, price, status, category,
                   description, goods_condition AS `condition`, story, floor_price,
                   scene, location, image, store_id, store_name, created_at
            FROM goods
            """;

    private final JdbcClient jdbc;

    public ProductService(JdbcClient jdbc) { this.jdbc = jdbc; }

    @Transactional
    public ProductView create(CreateProduct request) {
        long storeId = ensureStore(request.sellerId());
        jdbc.sql("""
                INSERT INTO goods(seller_id, name, price, status, store_id, store_name)
                SELECT :sellerId, :name, :price, 'ON_SALE', store_id, store_name
                FROM store WHERE store_id=:storeId
                """).param("sellerId", request.sellerId()).param("name", request.name())
                .param("price", request.price()).param("storeId", storeId).update();
        return jdbc.sql(PRODUCT_COLUMNS + " ORDER BY goods_id DESC LIMIT 1").query(ProductView.class).single();
    }

    public ProductView find(long id) {
        return jdbc.sql(PRODUCT_COLUMNS + " WHERE goods_id=:id").param("id", id)
                .query(ProductView.class).optional()
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "product not found"));
    }

    public List<ProductView> list() {
        return jdbc.sql(PRODUCT_COLUMNS + " ORDER BY goods_id").query(ProductView.class).list();
    }

    public List<ProductView> mine(long sellerId) {
        return jdbc.sql(PRODUCT_COLUMNS + " WHERE seller_id=:sellerId ORDER BY created_at DESC, goods_id DESC")
                .param("sellerId", sellerId).query(ProductView.class).list();
    }

    @Transactional
    public ProductView update(long id, UpdateProduct request, CatalogUser user) {
        ProductView existing = find(id);
        if (!user.isSeller()) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "only sellers can edit products");
        }
        if (existing.sellerId() != user.userId()) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "cannot edit another seller's product");
        }
        int changed = jdbc.sql("""
                UPDATE goods SET name=:name, category=:category, description=:description,
                    goods_condition=:condition, story=:story, price=:price, floor_price=:floorPrice,
                    location=:location, image=:image, updated_at=CURRENT_TIMESTAMP
                WHERE goods_id=:id AND seller_id=:sellerId
                """)
                .param("name", request.name()).param("category", request.category())
                .param("description", request.description()).param("condition", request.condition())
                .param("story", request.story()).param("price", request.price())
                .param("floorPrice", request.floorPrice()).param("location", request.location())
                .param("image", request.image()).param("id", id).param("sellerId", user.userId()).update();
        if (changed == 0) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "product was not updated");
        }
        return find(id);
    }

    public void markSold(long id, String orderNumber) {
        int changed = jdbc.sql("""
                UPDATE goods SET status='SOLD', sold_order_number=:orderNumber, updated_at=CURRENT_TIMESTAMP
                WHERE goods_id=:id AND (status='ON_SALE' OR sold_order_number=:orderNumber)
                """).param("id", id).param("orderNumber", orderNumber).update();
        if (changed == 0) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "product is not available");
        }
    }

    private long ensureStore(long sellerId) {
        return jdbc.sql("SELECT store_id FROM store WHERE seller_id=:sellerId ORDER BY store_id LIMIT 1")
                .param("sellerId", sellerId).query(Long.class).optional().orElseGet(() -> {
                    String sellerName = "卖家 " + sellerId;
                    jdbc.sql("""
                            INSERT INTO store(seller_id, seller_name, store_name, status)
                            VALUES (:sellerId, :sellerName, :storeName, 'normal')
                            """).param("sellerId", sellerId).param("sellerName", sellerName)
                            .param("storeName", sellerName + " 的店铺").update();
                    return jdbc.sql("SELECT store_id FROM store WHERE seller_id=:sellerId ORDER BY store_id LIMIT 1")
                            .param("sellerId", sellerId).query(Long.class).single();
                });
    }

    public record CreateProduct(long sellerId, String name, BigDecimal price) {}
    public record UpdateProduct(String name, String category, String description, String condition,
                                String story, BigDecimal price, BigDecimal floorPrice, String location, String image) {}
    public record ProductView(long productId, long sellerId, String name, BigDecimal price, String status,
                              String category, String description, String condition, String story,
                              BigDecimal floorPrice, String scene, String location, String image,
                              Long storeId, String storeName, LocalDateTime createdAt) {
        public String id() { return String.valueOf(productId); }
        public String title() { return name; }
        public String cover() { return image; }
        public long publisherId() { return sellerId; }
        public String shopName() { return storeName; }
        public String publishedAt() { return createdAt == null ? "" : createdAt.toString(); }
    }
}
