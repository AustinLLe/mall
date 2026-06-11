package com.example.shopping_back.shop.mapper;

import com.example.shopping_back.shop.model.TopicCommentRecord;
import com.example.shopping_back.shop.model.TopicPostRecord;
import com.example.shopping_back.shop.model.TopicRecord;
import java.util.List;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface ShopTopicMapper {
    @Update("""
            CREATE TABLE IF NOT EXISTS community_topic (
                topic_id INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
                type VARCHAR(60) NOT NULL,
                title VARCHAR(255) NOT NULL,
                topic_desc VARCHAR(1000) NOT NULL,
                author VARCHAR(80) DEFAULT '松果用户',
                cover TEXT DEFAULT NULL,
                tags VARCHAR(500) DEFAULT NULL,
                status VARCHAR(20) NOT NULL DEFAULT 'normal',
                created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
            ) ENGINE=InnoDB DEFAULT CHARACTER SET=utf8mb4
            """)
    void createTopicTable();

    @Update("""
            CREATE TABLE IF NOT EXISTS topic_post (
                post_id INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
                topic_id INT NOT NULL,
                user_id INT NOT NULL,
                product_id INT DEFAULT NULL,
                store_id INT DEFAULT NULL,
                content TEXT NOT NULL,
                images TEXT DEFAULT NULL,
                created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                INDEX idx_topic_post (topic_id, created_at)
            ) ENGINE=InnoDB DEFAULT CHARACTER SET=utf8mb4
            """)
    void createPostTable();

    @Update("""
            CREATE TABLE IF NOT EXISTS topic_comment (
                comment_id INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
                post_id INT NOT NULL,
                user_id INT NOT NULL,
                content VARCHAR(1000) NOT NULL,
                created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                INDEX idx_post_comment (post_id, created_at)
            ) ENGINE=InnoDB DEFAULT CHARACTER SET=utf8mb4
            """)
    void createCommentTable();

    @Update("""
            CREATE TABLE IF NOT EXISTS topic_post_like (
                id INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
                post_id INT NOT NULL,
                user_id INT NOT NULL,
                created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                UNIQUE KEY uk_topic_post_like (post_id, user_id)
            ) ENGINE=InnoDB DEFAULT CHARACTER SET=utf8mb4
            """)
    void createLikeTable();

    @Update("""
            CREATE TABLE IF NOT EXISTS topic_post_action (
                id INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
                post_id INT NOT NULL,
                user_id INT NOT NULL,
                action_type VARCHAR(20) NOT NULL,
                created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                UNIQUE KEY uk_topic_post_action (post_id, user_id, action_type),
                INDEX idx_topic_post_action (post_id, action_type)
            ) ENGINE=InnoDB DEFAULT CHARACTER SET=utf8mb4
            """)
    void createActionTable();

    @Select("SELECT COUNT(*) FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'topic_post' AND COLUMN_NAME = #{column}")
    int countPostColumn(@Param("column") String column);

    @Update("ALTER TABLE topic_post ADD COLUMN product_id INT DEFAULT NULL")
    void addPostProductIdColumn();

    @Update("ALTER TABLE topic_post ADD COLUMN store_id INT DEFAULT NULL")
    void addPostStoreIdColumn();

    @Select("""
            SELECT t.topic_id AS topicId,
                   t.type,
                   t.title,
                   t.topic_desc AS topicDesc,
                   t.author,
                   t.cover,
                   t.tags,
                   t.status,
                   t.created_at AS createdAt,
                   COUNT(DISTINCT p.post_id) AS postCount,
                   COUNT(DISTINCT l.id) AS likeCount
            FROM community_topic t
            LEFT JOIN topic_post p ON p.topic_id = t.topic_id
            LEFT JOIN topic_post_like l ON l.post_id = p.post_id
            WHERE t.status = 'normal'
              AND (#{tag} IS NULL OR #{tag} = '' OR FIND_IN_SET(#{tag}, REPLACE(t.tags, '，', ',')))
              AND (#{keyword} IS NULL OR #{keyword} = ''
                   OR t.title LIKE CONCAT('%', #{keyword}, '%')
                   OR t.topic_desc LIKE CONCAT('%', #{keyword}, '%')
                   OR t.type LIKE CONCAT('%', #{keyword}, '%')
                   OR t.tags LIKE CONCAT('%', #{keyword}, '%')
                   OR t.author LIKE CONCAT('%', #{keyword}, '%'))
            GROUP BY t.topic_id, t.type, t.title, t.topic_desc, t.author, t.cover, t.tags, t.status, t.created_at
            ORDER BY t.topic_id ASC
            """)
    List<TopicRecord> selectTopics(@Param("tag") String tag, @Param("keyword") String keyword);

    @Select("""
            SELECT t.topic_id AS topicId,
                   t.type,
                   t.title,
                   t.topic_desc AS topicDesc,
                   t.author,
                   t.cover,
                   t.tags,
                   t.status,
                   t.created_at AS createdAt,
                   COUNT(DISTINCT p.post_id) AS postCount,
                   COUNT(DISTINCT l.id) AS likeCount
            FROM community_topic t
            LEFT JOIN topic_post p ON p.topic_id = t.topic_id
            LEFT JOIN topic_post_like l ON l.post_id = p.post_id
            WHERE t.topic_id = #{topicId}
            GROUP BY t.topic_id, t.type, t.title, t.topic_desc, t.author, t.cover, t.tags, t.status, t.created_at
            """)
    TopicRecord selectTopic(@Param("topicId") Integer topicId);

    @Insert("""
            INSERT INTO community_topic(type, title, topic_desc, author, cover, tags, status)
            VALUES(#{type}, #{title}, #{topicDesc}, #{author}, #{cover}, #{tags}, 'normal')
            """)
    @Options(useGeneratedKeys = true, keyProperty = "topicId", keyColumn = "topic_id")
    int insertTopic(TopicRecord topic);

    @Select("""
            SELECT p.post_id AS postId,
                   p.topic_id AS topicId,
                   p.user_id AS userId,
                   p.product_id AS productId,
                   p.store_id AS storeId,
                   COALESCE(u.username, '松果用户') AS username,
                   p.content,
                   p.images,
                   p.created_at AS createdAt,
                   COUNT(DISTINCT l.id) AS likeCount,
                   COUNT(DISTINCT c.comment_id) AS commentCount,
                   COUNT(DISTINCT CASE WHEN l.user_id = #{currentUserId} THEN l.id END) AS liked,
                   COUNT(DISTINCT CASE WHEN a.action_type = 'want' THEN a.id END) AS wantCount,
                   COUNT(DISTINCT CASE WHEN a.action_type = 'collect' THEN a.id END) AS collectCount,
                   COUNT(DISTINCT CASE WHEN a.user_id = #{currentUserId} AND a.action_type = 'want' THEN a.id END) AS wanted,
                   COUNT(DISTINCT CASE WHEN a.user_id = #{currentUserId} AND a.action_type = 'collect' THEN a.id END) AS collected
            FROM topic_post p
            LEFT JOIN users u ON u.user_id = p.user_id
            LEFT JOIN topic_post_like l ON l.post_id = p.post_id
            LEFT JOIN topic_comment c ON c.post_id = p.post_id
            LEFT JOIN topic_post_action a ON a.post_id = p.post_id
            WHERE p.topic_id = #{topicId}
            GROUP BY p.post_id, p.topic_id, p.user_id, p.product_id, p.store_id, u.username, p.content, p.images, p.created_at
            ORDER BY p.created_at DESC, p.post_id DESC
            """)
    List<TopicPostRecord> selectPosts(@Param("topicId") Integer topicId, @Param("currentUserId") Integer currentUserId);

    @Select("""
            SELECT c.comment_id AS commentId,
                   c.post_id AS postId,
                   c.user_id AS userId,
                   COALESCE(u.username, '松果用户') AS username,
                   c.content,
                   c.created_at AS createdAt
            FROM topic_comment c
            LEFT JOIN users u ON u.user_id = c.user_id
            WHERE c.post_id = #{postId}
            ORDER BY c.created_at ASC, c.comment_id ASC
            """)
    List<TopicCommentRecord> selectComments(@Param("postId") Integer postId);

    @Insert("""
            INSERT INTO topic_post(topic_id, user_id, product_id, store_id, content, images)
            VALUES(#{topicId}, #{userId}, #{productId}, #{storeId}, #{content}, #{images})
            """)
    int insertPost(
            @Param("topicId") Integer topicId,
            @Param("userId") Integer userId,
            @Param("productId") Integer productId,
            @Param("storeId") Integer storeId,
            @Param("content") String content,
            @Param("images") String images);

    @Insert("INSERT INTO topic_comment(post_id, user_id, content) VALUES(#{postId}, #{userId}, #{content})")
    int insertComment(@Param("postId") Integer postId, @Param("userId") Integer userId, @Param("content") String content);

    @Select("SELECT COUNT(*) FROM topic_post_like WHERE post_id = #{postId} AND user_id = #{userId}")
    int likeExists(@Param("postId") Integer postId, @Param("userId") Integer userId);

    @Insert("INSERT IGNORE INTO topic_post_like(post_id, user_id) VALUES(#{postId}, #{userId})")
    int insertLike(@Param("postId") Integer postId, @Param("userId") Integer userId);

    @Delete("DELETE FROM topic_post_like WHERE post_id = #{postId} AND user_id = #{userId}")
    int deleteLike(@Param("postId") Integer postId, @Param("userId") Integer userId);

    @Select("SELECT topic_id FROM topic_post WHERE post_id = #{postId}")
    Integer topicIdByPost(@Param("postId") Integer postId);

    @Select("SELECT COUNT(*) FROM topic_post_action WHERE post_id = #{postId} AND user_id = #{userId} AND action_type = #{actionType}")
    int actionExists(@Param("postId") Integer postId, @Param("userId") Integer userId, @Param("actionType") String actionType);

    @Insert("INSERT IGNORE INTO topic_post_action(post_id, user_id, action_type) VALUES(#{postId}, #{userId}, #{actionType})")
    int insertAction(@Param("postId") Integer postId, @Param("userId") Integer userId, @Param("actionType") String actionType);

    @Delete("DELETE FROM topic_post_action WHERE post_id = #{postId} AND user_id = #{userId} AND action_type = #{actionType}")
    int deleteAction(@Param("postId") Integer postId, @Param("userId") Integer userId, @Param("actionType") String actionType);
}
