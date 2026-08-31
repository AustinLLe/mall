package com.example.mall.catalog;

import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.List;

@Service
public class StoreService {
    private static final String STORE_COLUMNS = """
            SELECT s.store_id, s.seller_id, s.seller_name, s.store_name, s.score,
                   s.credit_score, s.store_desc, s.badge, s.service_tags,
                   (SELECT COUNT(*) FROM follow_store f WHERE f.store_id=s.store_id) AS follower_count,
                   COUNT(g.goods_id) AS product_count,
                   COALESCE(SUM(CASE WHEN g.scene='new' THEN 1 ELSE 0 END), 0) AS new_count,
                   COALESCE(SUM(CASE WHEN g.scene='used' THEN 1 ELSE 0 END), 0) AS used_count
            FROM store s LEFT JOIN goods g ON g.store_id=s.store_id AND g.status='ON_SALE'
            """;
    private static final String GROUPING = " GROUP BY s.store_id, s.seller_id, s.seller_name, s.store_name, s.score, s.credit_score, s.store_desc, s.badge, s.service_tags";
    private final JdbcClient jdbc;

    public StoreService(JdbcClient jdbc) { this.jdbc = jdbc; }

    public List<StoreView> list() {
        return jdbc.sql(STORE_COLUMNS + " WHERE s.status='normal'" + GROUPING + " ORDER BY s.store_id")
                .query(StoreView.class).list();
    }

    public StoreView find(long id) {
        return jdbc.sql(STORE_COLUMNS + " WHERE s.store_id=:id AND s.status='normal'" + GROUPING)
                .param("id", id).query(StoreView.class).optional()
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "store not found"));
    }

    public StoreView mine(long sellerId) {
        return jdbc.sql(STORE_COLUMNS + " WHERE s.seller_id=:sellerId AND s.status='normal'" + GROUPING + " ORDER BY s.store_id LIMIT 1")
                .param("sellerId", sellerId).query(StoreView.class).optional()
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "store not found"));
    }

    public List<ProductService.ProductView> products(long storeId) {
        find(storeId);
        return jdbc.sql(ProductService.PRODUCT_COLUMNS + " WHERE store_id=:storeId AND status='ON_SALE' ORDER BY created_at DESC, goods_id DESC")
                .param("storeId", storeId).query(ProductService.ProductView.class).list();
    }

    public record StoreView(long storeId, long sellerId, String sellerName, String storeName,
                            BigDecimal score, int creditScore, String storeDesc, String badge,
                            String serviceTags, long followerCount, long productCount, long newCount, long usedCount) {
        public String id() { return String.valueOf(storeId); }
        public String name() { return storeName; }
        public String desc() { return storeDesc; }
        public String fans() { return String.valueOf(followerCount); }
    }
}
