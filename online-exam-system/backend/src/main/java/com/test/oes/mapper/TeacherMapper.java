package com.test.oes.mapper;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.test.oes.entity.Teacher;
import org.apache.ibatis.annotations.*;

@Mapper
public interface TeacherMapper {

    IPage<Teacher> findAll(Page<Teacher> page, @Param("teacherId") String teacherId,
                           @Param("teacherName") String teacherName, @Param("institute") String institute,
                           @Param("type") String type, @Param("tel") String tel, @Param("email") String email);

    Teacher findById(Integer teacherId);

    int deleteById(Integer teacherId);

    int update(Teacher teacher);

    int add(Teacher teacher);

    int countByCardId(@Param("cid") String cid);

    int countByCardIdExcludingTeacher(@Param("cid") String cid, @Param("teacherId") int teacherId);
}
