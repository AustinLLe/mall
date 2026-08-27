package com.example.shopping_back.cart;

import java.util.List;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface CartMapper {
    @Update("""
            CREATE TABLE IF NOT EXISTS cart_item (
                cart_id INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
                user_id INT NOT NULL,
                goods_id INT NOT NULL,
                quantity INT NOT NULL DEFAULT 1,
                selected TINYINT(1) NOT NULL DEFAULT 1,
                created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                UNIQUE KEY uk_cart_user_goods (user_id, goods_id)
            ) ENGINE=InnoDB DEFAULT CHARACTER SET=utf8mb4
            """)
    void createTable();

    @Select("""
            SELECT c.cart_id AS cartId,
                   c.user_id AS userId,
                   c.goods_id AS goodsId,
                   c.quantity,
                   c.selected,
                   g.goods_name AS goodsName,
                   g.image,
                   g.price
            FROM cart_item c
            LEFT JOIN goods g ON g.goods_id = c.goods_id
            WHERE c.user_id = #{userId}
            ORDER BY c.created_at DESC, c.cart_id DESC
            """)
    List<CartItemRecord> selectItems(@Param("userId") Integer userId);

    @Insert("""
            INSERT INTO cart_item(user_id, goods_id, quantity, selected)
            VALUES(#{userId}, #{goodsId}, #{quantity}, 1)
            ON DUPLICATE KEY UPDATE quantity = quantity + #{quantity}
            """)
    int addItem(@Param("userId") Integer userId,
                @Param("goodsId") Integer goodsId,
                @Param("quantity") Integer quantity);

    @Select("SELECT COUNT(*) FROM cart_item WHERE cart_id = #{cartId} AND user_id = #{userId}")
    int countByCartId(@Param("cartId") Integer cartId, @Param("userId") Integer userId);

    @Update("UPDATE cart_item SET quantity = #{quantity} WHERE cart_id = #{cartId} AND user_id = #{userId}")
    int updateQuantity(@Param("cartId") Integer cartId,
                       @Param("userId") Integer userId,
                       @Param("quantity") Integer quantity);

    @Update("UPDATE cart_item SET selected = #{selected} WHERE cart_id = #{cartId} AND user_id = #{userId}")
    int updateSelected(@Param("cartId") Integer cartId,
                       @Param("userId") Integer userId,
                       @Param("selected") boolean selected);

    @Delete("DELETE FROM cart_item WHERE cart_id = #{cartId} AND user_id = #{userId}")
    int deleteItem(@Param("cartId") Integer cartId, @Param("userId") Integer userId);
}
