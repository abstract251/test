package com.test.oes.mapper;

import com.test.oes.entity.Admin;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface AdminMapper {

    List<Admin> findAll();

    Admin findById(Integer adminId);

    Admin findByCardId(@Param("cardId") String cardId);

    int deleteById(Integer adminId);

    int update(Admin admin);

    int add(Admin admin);

    int countByCardId(@Param("cid") String cid);

    int countByCardIdExcludingAdmin(@Param("cid") String cid, @Param("adminId") int adminId);

}
