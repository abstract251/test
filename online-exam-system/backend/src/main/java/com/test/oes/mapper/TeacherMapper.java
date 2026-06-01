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

    @Select("select teacherId, teacherName, sex, tel, email, cardId, role, institute, type from teacher where teacherId = #{teacherId}")
    Teacher findById(Integer teacherId);

    @Select("select teacherId, teacherName, pwd from teacher where teacherId = #{teacherId}")
    Teacher findForLogin(String teacherId);

    @Delete("delete from teacher where teacherId = #{teacherId}")
    int deleteById(Integer teacherId);

    @Update("<script>" +
            "update teacher set teacherName = #{teacherName},sex = #{sex}," +
            "tel = #{tel}, email = #{email}," +
            "<if test='pwd != null and pwd != \"\"'>pwd = #{pwd},</if>" +
            "cardId = #{cardId},role = #{role},institute = #{institute},type = #{type} where teacherId = #{teacherId}" +
            "</script>")
    int update(Teacher teacher);

    @Update("update teacher set teacherName = #{teacherName},sex = #{sex}," +
            "tel = #{tel}, email = #{email},cardId = #{cardId}," +
            "role = #{role},institute = #{institute},type = #{type} where teacherId = #{teacherId}")
    int updateWithoutPassword(Teacher teacher);

    @Options(useGeneratedKeys = true,keyProperty = "teacherId")
    @Insert("insert into teacher(teacherName,sex,tel,email,pwd,cardId,role,type,institute) " +
            "values(#{teacherName},#{sex},#{tel},#{email},#{pwd},#{cardId},#{role},#{type},#{institute})")
    int add(Teacher teacher);

    @Select("SELECT COUNT(*) FROM teacher WHERE cardId = #{cid} AND cardId IS NOT NULL AND cardId <> ''")
    int countByCardId(@Param("cid") String cid);

    @Select("SELECT COUNT(*) FROM teacher WHERE cardId = #{cid} AND cardId IS NOT NULL AND cardId <> '' "
            + "AND teacherId <> #{teacherId}")
    int countByCardIdExcludingTeacher(@Param("cid") String cid, @Param("teacherId") int teacherId);
}
