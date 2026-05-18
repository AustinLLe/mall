package com.example.shopping_back.auth.mapper;

import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import com.example.shopping_back.auth.model.StoredUser;

@Mapper
public interface UserMapper {
    @Insert("INSERT INTO users(username, password_hash, phone) " +
            "VALUES(#{username}, #{passwordHash}, #{phone})")
    @Options(useGeneratedKeys = true, keyProperty = "userId", keyColumn = "user_id")
    int insertUser(StoredUser user);

    @Select("SELECT user_id AS userId, username, password_hash as passwordHash, phone, credit FROM users WHERE username = #{username}")
    StoredUser findByUsername(String username);

    @Select("SELECT user_id as userId, username, phone, credit FROM users WHERE username LIKE CONCAT('%', #{keyword}, '%')")
    List<StoredUser> searchUsersByKeyword(@Param("keyword") String keyword);
}