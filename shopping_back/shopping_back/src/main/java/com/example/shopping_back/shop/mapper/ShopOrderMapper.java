package com.example.shopping_back.shop.mapper;

import com.example.shopping_back.shop.model.ProductReviewRecord;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface ShopOrderMapper {
    @Update("""
            CREATE TABLE IF NOT EXISTS product_review (
                review_id INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
                order_id INT NOT NULL,
                goods_id INT NOT NULL,
                buyer_id INT NOT NULL,
                seller_id INT NOT NULL,
                product_score INT NOT NULL,
                seller_score INT NOT NULL,
                content VARCHAR(1000) NOT NULL,
                created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                UNIQUE KEY uk_review_order (order_id),
                INDEX idx_review_goods (goods_id, created_at),
                INDEX idx_review_seller (seller_id)
            ) ENGINE=InnoDB DEFAULT CHARACTER SET=utf8mb4
            """)
    void createReviewTable();

    @Select("""
            SELECT r.review_id AS reviewId,
                   r.order_id AS orderId,
                   r.goods_id AS goodsId,
                   r.buyer_id AS buyerId,
                   r.seller_id AS sellerId,
                   r.product_score AS productScore,
                   r.seller_score AS sellerScore,
                   r.content,
                   r.created_at AS createdAt,
                   COALESCE(u.username, '买家') AS buyerName
            FROM product_review r
            LEFT JOIN users u ON u.user_id = r.buyer_id
            WHERE r.goods_id = #{goodsId}
            ORDER BY r.created_at DESC, r.review_id DESC
            LIMIT 20
            """)
    List<ProductReviewRecord> selectProductReviews(@Param("goodsId") Integer goodsId);
}
