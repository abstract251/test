package com.test.oes.mapper;

import com.test.oes.entity.AuthRefreshToken;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface AuthRefreshTokenMapper {

    @Options(useGeneratedKeys = true, keyProperty = "id")
    @Insert("insert into auth_refresh_token(jti, user_id, role, username, expires_at, revoked, created_at) " +
            "values(#{jti}, #{userId}, #{role}, #{username}, #{expiresAt}, #{revoked}, #{createdAt})")
    int insert(AuthRefreshToken token);

    @Select("select id, jti, user_id as userId, role, username, expires_at as expiresAt, revoked, created_at as createdAt " +
            "from auth_refresh_token where jti = #{jti} limit 1")
    AuthRefreshToken findByJti(@Param("jti") String jti);

    @Update("update auth_refresh_token set revoked = 1 where id = #{id}")
    int revokeById(@Param("id") Long id);

    @Update("update auth_refresh_token set revoked = 1 where id = #{id} and revoked = 0")
    int revokeIfActiveById(@Param("id") Long id);
}
