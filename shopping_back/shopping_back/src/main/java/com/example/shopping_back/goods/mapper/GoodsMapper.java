package com.example.shopping_back.goods.mapper;

import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import com.example.shopping_back.auth.model.StoredUser;
import com.example.shopping_back.goods.model.Goods;

@Mapper
public interface GoodsMapper {
    @Insert("INSERT INTO goods (seller_id, goods_name, goods_desc, price, scene, address, image, status)" +
            "VALUES (#{sellerId}, #{goodsName}, #{goodsDesc}, #{price}, #{scene}, #{address}, #{image}, '0')")
    @Options(useGeneratedKeys = true, keyProperty = "goodsId", keyColumn = "goods_id")
    int insertGoods(Goods goods);

    @Update("UPDATE goods SET status = #{status} WHERE goods_id = #{goodsId}")
    int updateGoodsStatus(@Param("goodsId") Integer goodsId, @Param("status") String status);

    //查询所有在售商品列表（首页/猜你喜欢）

    @Select("SELECT goods_id AS goodsId, seller_id AS sellerId, goods_name AS goodsName, goods_desc AS goodsDesc, price, scene, address, image, status, create_time AS createTime " +
            "FROM goods " +
            "WHERE status = '0' " +
            "ORDER BY create_time DESC")
    List<Goods> selectAllActiveGoods();

    
    @Select("SELECT goods_id AS goodsId, seller_id AS sellerId, goods_name AS goodsName, goods_desc AS goodsDesc, price, scene, address, image, status " +
            "FROM goods " +
            "WHERE goods_id = #{goodsId}")
    Goods selectGoodsById(final Integer goodsId);

    @Select("SELECT g.goods_id AS goodsId, g.seller_id AS sellerId, g.goods_name AS goodsName, " +
            "g.goods_desc AS goodsDesc, g.price, g.scene, g.address, g.image, g.status, " +
            "g.create_time AS createTime, " +
            "(CASE " +
            "  WHEN g.goods_name LIKE CONCAT('%', #{keyword}, '%') THEN 3 " + 
            "  WHEN g.goods_desc LIKE CONCAT('%', #{keyword}, '%') THEN 2 " + 
            "  ELSE 1 " +                                                   
            " END) AS match_score " +
            "FROM goods g " +
            "LEFT JOIN users u ON g.seller_id = u.user_id " +
            "WHERE g.status = '0' AND ( " +
            "    g.goods_name LIKE CONCAT('%', #{keyword}, '%') OR " +
            "    g.goods_desc LIKE CONCAT('%', #{keyword}, '%') OR " +
            "    u.username LIKE CONCAT('%', #{keyword}, '%') " +
            ") " +
            "ORDER BY match_score DESC, g.${sortBy} ${sortOrder}")
    List<Goods> searchGoodsGlobal(@Param("keyword") String keyword, 
                                  @Param("sortBy") String sortBy, 
                                  @Param("sortOrder") String sortOrder);
    
}
