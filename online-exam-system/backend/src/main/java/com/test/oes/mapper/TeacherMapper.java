package com.test.oes.mapper;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.test.oes.entity.Teacher;
import org.apache.ibatis.annotations.*;

@Mapper
public interface TeacherMapper {

    @Select("select * from teacher where " +
            "cast(teacherId as char) like concat('%',#{teacherId},'%') " +
            "and ifnull(teacherName, '') like concat('%',#{teacherName},'%') " +
            "and ifnull(institute, '') like concat('%',#{institute},'%') " +
            "and ifnull(type, '') like concat('%',#{type},'%') " +
            "and ifnull(tel, '') like concat('%',#{tel},'%') " +
            "and ifnull(email, '') like concat('%',#{email},'%')")
    IPage<Teacher> findAll(Page<Teacher> page, @Param("teacherId") String teacherId,
                           @Param("teacherName") String teacherName, @Param("institute") String institute,
                           @Param("type") String type, @Param("tel") String tel, @Param("email") String email);

    @Select("select * from teacher where teacherId = #{teacherId}")
    Teacher findById(Integer teacherId);

    @Delete("delete from teacher where teacherId = #{teacherId}")
    int deleteById(Integer teacherId);

    @Update("update teacher set teacherName = #{teacherName},sex = #{sex}," +
            "tel = #{tel}, email = #{email},pwd = #{pwd},cardId = #{cardId}," +
            "role = #{role},institute = #{institute},type = #{type} where teacherId = #{teacherId}")
    int update(Teacher teacher);

    @Options(useGeneratedKeys = true,keyProperty = "teacherId")
    @Insert("insert into teacher(teacherName,sex,tel,email,pwd,cardId,role,type,institute) " +
            "values(#{teacherName},#{sex},#{tel},#{email},#{pwd},#{cardId},#{role},#{type},#{institute})")
    int add(Teacher teacher);
}
