package com.example.shopping_back.chat.mapper;

import com.example.shopping_back.chat.model.ChatMessage;
import org.apache.ibatis.annotations.*;
import java.util.List;

@Mapper
public interface ChatMessageMapper {

    @Insert("INSERT INTO chat_message (cov_id, sender_id, content, price_value, create_time, is_read, type) " +
            "VALUES (#{covId}, #{senderId}, #{content}, #{priceValue}, #{createTime}, #{isRead}, #{type})")
    @Options(useGeneratedKeys = true, keyProperty = "cmId", keyColumn = "cm_id")
    int insert(ChatMessage msg);

    @Select("SELECT cm_id, cov_id, sender_id, content, price_value, create_time, is_read, type " +
            "FROM chat_message WHERE cm_id = #{cmId}")
    @Results(id = "ChatMessageMap", value = {
            @Result(property = "cmId", column = "cm_id", id = true),
            @Result(property = "covId", column = "cov_id"),
            @Result(property = "senderId", column = "sender_id"),
            @Result(property = "content", column = "content"),
            @Result(property = "priceValue", column = "price_value"),
            @Result(property = "createTime", column = "create_time"),
            @Result(property = "isRead", column = "is_read"),
            @Result(property = "type", column = "type")
    })
    ChatMessage selectById(Integer cmId);

    @Select("SELECT cm_id, cov_id, sender_id, content, price_value, create_time, is_read, type " +
            "FROM chat_message WHERE cov_id = #{covId} ORDER BY create_time")
    @ResultMap("ChatMessageMap")
    List<ChatMessage> selectByCovId(Integer covId);

    @Update("UPDATE chat_message SET cov_id=#{covId}, sender_id=#{senderId}, content=#{content}, " +
            "price_value=#{priceValue}, create_time=#{createTime}, is_read=#{isRead}, type=#{type} WHERE cm_id=#{cmId}")
    int update(ChatMessage msg);

    @Delete("DELETE FROM chat_message WHERE cm_id=#{cmId}")
    int deleteById(Integer cmId);

    @Select("SELECT * FROM chat_message WHERE cov_id = #{covId} ORDER BY create_time DESC LIMIT #{limit}")
    @ResultMap("ChatMessageMap")
    List<ChatMessage> findRecentByCovId(@Param("covId") Integer covId, @Param("limit") int limit);

    @Select("SELECT * FROM chat_message WHERE cov_id = #{covId} ORDER BY create_time DESC LIMIT 1")
    @ResultMap("ChatMessageMap")
    ChatMessage findLastByCovId(@Param("covId") Integer covId);

    @Select("SELECT COUNT(*) FROM chat_message WHERE cov_id = #{covId} AND sender_id != #{userId} AND is_read = 0")
    int countUnreadByCovId(@Param("covId") Integer covId, @Param("userId") Integer userId);

    @Update("UPDATE chat_message SET is_read = 1 WHERE cov_id = #{covId} AND sender_id != #{userId} AND is_read = 0")
    void markAllAsRead(@Param("covId") Integer covId, @Param("userId") Integer userId);
}

