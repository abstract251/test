package com.test.oes.mapper;

import com.test.oes.entity.Admin;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface AdminMapper {

    @Select("select adminName,sex,tel,email,cardId,role from `admin`")
    List<Admin> findAll();

    @Select("select adminId,adminName,sex,tel,email,cardId,role,pwd from `admin` where adminId = #{adminId}")
    Admin findById(Integer adminId);

    @Delete("delete from `admin` where adminId = #{adminId}")
    int deleteById(Integer adminId);

    @Update("update `admin` set adminName = #{adminName},sex = #{sex}," +
            "tel = #{tel}, email = #{email},pwd = #{pwd},cardId = #{cardId},role = #{role} where adminId = #{adminId}")
    int update(Admin admin);

    @Options(useGeneratedKeys = true,keyProperty = "adminId")
    @Insert("insert into `admin`(adminName,sex,tel,email,pwd,cardId,role) " +
            "values(#{adminName},#{sex},#{tel},#{email},#{pwd},#{cardId},#{role})")
    int add(Admin admin);

}
