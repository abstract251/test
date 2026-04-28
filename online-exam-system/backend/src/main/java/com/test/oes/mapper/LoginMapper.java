package com.test.oes.mapper;

import com.test.oes.entity.Admin;
import com.test.oes.entity.Student;
import com.test.oes.entity.Teacher;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface LoginMapper {

    Admin adminLogin(@Param("username") Integer username, @Param("password") String password);

    Teacher teacherLogin(@Param("username") Integer username, @Param("password") String password);

    Student studentLogin(@Param("username") Integer username, @Param("password") String password);
}
