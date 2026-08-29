package com.example.catalogservice.mapper;

import com.example.catalogservice.model.StoreRecord;
import java.util.List;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Result;
import org.apache.ibatis.annotations.ResultMap;
import org.apache.ibatis.annotations.Results;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface ShopStoreMapper {
    String STORE_SELECT = """
            SELECT s.store_id AS storeId,
                   s.seller_id AS sellerId,
                   COALESCE(u.username, '卖家') AS sellerName,
                   u.avatar_url AS sellerAvatar,
                   s.store_name AS storeName,
                   s.status,
                   s.score,
                   s.credit_score AS creditScore,
                   s.violation_count AS violationCount,
                   s.store_desc AS storeDesc,
                   s.badge,
                   s.service_tags AS serviceTags,
                   s.created_at AS createdAt
            FROM store s
            LEFT JOIN users u ON u.user_id = s.seller_id
            """;

    @Select("SELECT COUNT(*) FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'store' AND COLUMN_NAME = #{column}")
    int countStoreColumn(@Param("column") String column);

    @Update("ALTER TABLE store ADD COLUMN store_desc VARCHAR(500) DEFAULT NULL")
    void addStoreDescColumn();

    @Update("ALTER TABLE store ADD COLUMN badge VARCHAR(60) DEFAULT NULL")
    void addBadgeColumn();

    @Update("ALTER TABLE store ADD COLUMN service_tags VARCHAR(255) DEFAULT NULL")
    void addServiceTagsColumn();

    @Select("SELECT COUNT(*) FROM information_schema.STATISTICS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'follow_store' AND INDEX_NAME = 'uk_follow_user_store'")
    int countFollowUniqueIndex();

    @Update("DELETE f1 FROM follow_store f1 JOIN follow_store f2 ON f1.user_id = f2.user_id AND f1.store_id = f2.store_id AND f1.id > f2.id")
    int dedupeFollows();

    @Update("ALTER TABLE follow_store ADD UNIQUE KEY uk_follow_user_store (user_id, store_id)")
    void addFollowUniqueIndex();

    @Select(STORE_SELECT + " WHERE s.status = 'normal' ORDER BY s.store_id")
    @Results(id = "StoreMap", value = {
            @Result(property = "storeId", column = "storeId", id = true),
            @Result(property = "sellerId", column = "sellerId"),
            @Result(property = "sellerName", column = "sellerName"),
            @Result(property = "sellerAvatar", column = "sellerAvatar"),
            @Result(property = "storeName", column = "storeName"),
            @Result(property = "status", column = "status"),
            @Result(property = "score", column = "score"),
            @Result(property = "creditScore", column = "creditScore"),
            @Result(property = "violationCount", column = "violationCount"),
            @Result(property = "storeDesc", column = "storeDesc"),
            @Result(property = "badge", column = "badge"),
            @Result(property = "serviceTags", column = "serviceTags"),
            @Result(property = "createdAt", column = "createdAt")
    })
    List<StoreRecord> selectNormalStores();

    @Select(STORE_SELECT + " WHERE s.store_id = #{storeId} LIMIT 1")
    @Results(id = "StoreMapInline", value = {
            @Result(property = "storeId", column = "storeId", id = true),
            @Result(property = "sellerId", column = "sellerId"),
            @Result(property = "sellerName", column = "sellerName"),
            @Result(property = "sellerAvatar", column = "sellerAvatar"),
            @Result(property = "storeName", column = "storeName"),
            @Result(property = "status", column = "status"),
            @Result(property = "score", column = "score"),
            @Result(property = "creditScore", column = "creditScore"),
            @Result(property = "violationCount", column = "violationCount"),
            @Result(property = "storeDesc", column = "storeDesc"),
            @Result(property = "badge", column = "badge"),
            @Result(property = "serviceTags", column = "serviceTags"),
            @Result(property = "createdAt", column = "createdAt")
    })
    StoreRecord selectById(@Param("storeId") Integer storeId);

    @Select(STORE_SELECT + " WHERE s.store_name = #{storeName} ORDER BY s.store_id LIMIT 1")
    @Results(id = "StoreMapByName", value = {
            @Result(property = "storeId", column = "storeId", id = true),
            @Result(property = "sellerId", column = "sellerId"),
            @Result(property = "sellerName", column = "sellerName"),
            @Result(property = "sellerAvatar", column = "sellerAvatar"),
            @Result(property = "storeName", column = "storeName"),
            @Result(property = "status", column = "status"),
            @Result(property = "score", column = "score"),
            @Result(property = "creditScore", column = "creditScore"),
            @Result(property = "violationCount", column = "violationCount"),
            @Result(property = "storeDesc", column = "storeDesc"),
            @Result(property = "badge", column = "badge"),
            @Result(property = "serviceTags", column = "serviceTags"),
            @Result(property = "createdAt", column = "createdAt")
    })
    StoreRecord selectByName(@Param("storeName") String storeName);

    @Select(STORE_SELECT + " WHERE s.seller_id = #{sellerId} ORDER BY s.store_id DESC LIMIT 1")
    @ResultMap("StoreMapInline")
    StoreRecord selectBySeller(@Param("sellerId") Integer sellerId);

    @Insert("""
            INSERT INTO store(seller_id, store_name, status, score, credit_score, violation_count, store_desc, badge, service_tags)
            VALUES(#{sellerId}, #{storeName}, 'normal', 4.8, 100, 0, #{storeDesc}, #{badge}, #{serviceTags})
            """)
    int insertSellerStore(@Param("sellerId") Integer sellerId,
                          @Param("storeName") String storeName,
                          @Param("storeDesc") String storeDesc,
                          @Param("badge") String badge,
                          @Param("serviceTags") String serviceTags);

    @Update("""
            UPDATE store
            SET store_name = #{storeName},
                store_desc = #{storeDesc},
                badge = #{badge},
                service_tags = #{serviceTags}
            WHERE store_id = #{storeId} AND seller_id = #{sellerId}
            """)
    int updateSellerStore(@Param("storeId") Integer storeId,
                          @Param("sellerId") Integer sellerId,
                          @Param("storeName") String storeName,
                          @Param("storeDesc") String storeDesc,
                          @Param("badge") String badge,
                          @Param("serviceTags") String serviceTags);

    @Select("SELECT COUNT(*) FROM follow_store WHERE store_id = #{storeId}")
    int followerCount(@Param("storeId") Integer storeId);

    @Select("SELECT COUNT(*) FROM follow_store WHERE user_id = #{userId} AND store_id = #{storeId}")
    int isFollowed(@Param("userId") Integer userId, @Param("storeId") Integer storeId);

    @Insert("INSERT IGNORE INTO follow_store(user_id, store_id, store_name) VALUES(#{userId}, #{storeId}, #{storeName})")
    int follow(@Param("userId") Integer userId, @Param("storeId") Integer storeId, @Param("storeName") String storeName);

    @Delete("DELETE FROM follow_store WHERE user_id = #{userId} AND store_id = #{storeId}")
    int unfollow(@Param("userId") Integer userId, @Param("storeId") Integer storeId);
}
