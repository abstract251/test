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

    @Select("select adminId,adminName,cardId from `admin` where cardId = #{cardId} limit 1")
    Admin findByCardId(@Param("cardId") String cardId);

    @Delete("delete from `admin` where adminId = #{adminId}")
    int deleteById(Integer adminId);

    @Update("update `admin` set adminName = #{adminName},sex = #{sex}," +
            "tel = #{tel}, email = #{email},pwd = #{pwd},cardId = #{cardId},role = #{role} where adminId = #{adminId}")
    int update(Admin admin);

    @Update("update `admin` set adminName = #{adminName},sex = #{sex}," +
            "tel = #{tel}, email = #{email},cardId = #{cardId},role = #{role} where adminId = #{adminId}")
    int updateWithoutPassword(Admin admin);

    @Options(useGeneratedKeys = true,keyProperty = "adminId")
    @Insert("insert into `admin`(adminName,sex,tel,email,pwd,cardId,role) " +
            "values(#{adminName},#{sex},#{tel},#{email},#{pwd},#{cardId},#{role})")
    int add(Admin admin);

    @Select("SELECT COUNT(*) FROM `admin` WHERE cardId = #{cid} AND cardId IS NOT NULL AND cardId <> ''")
    int countByCardId(@Param("cid") String cid);

    @Select("SELECT COUNT(*) FROM `admin` WHERE cardId = #{cid} AND cardId IS NOT NULL AND cardId <> '' "
            + "AND adminId <> #{adminId}")
    int countByCardIdExcludingAdmin(@Param("cid") String cid, @Param("adminId") int adminId);

}
