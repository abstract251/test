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

    int insert(AuthRefreshToken token);

    AuthRefreshToken findByJti(@Param("jti") String jti);

    int revokeById(@Param("id") Long id);

    int revokeIfActiveById(@Param("id") Long id);
}
