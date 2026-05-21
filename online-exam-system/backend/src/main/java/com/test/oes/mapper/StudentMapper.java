package com.test.oes.mapper;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.test.oes.entity.Student;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface StudentMapper {

    /**
     * 分页查询所有学生
     * @param page 传递参数
     * @return List<Student>
     */
    @Select("select * from student where " +
            "cast(studentId as char) like concat('%',#{studentId},'%') " +
            "and " +
            "ifnull(studentName, '') like concat('%',#{name},'%') " +
            "and ifnull(grade, '') like concat('%',#{grade},'%') " +
            "and ifnull(tel, '') like concat('%',#{tel},'%') " +
            "and ifnull(major, '') like concat('%',#{major},'%') " +
            "and ifnull(institute, '') like concat('%',#{institute},'%') " +
            "and ifnull(clazz, '') like concat('%',#{clazz},'%')")
    IPage<Student> findAll(Page<Student> page, @Param("studentId") String studentId,
                           @Param("name") String name, @Param("grade") String grade,
                           @Param("tel") String tel,  @Param("institute") String institute,
                           @Param("major")String major, @Param("clazz") String clazz);

    @Select("select * from student where studentId = #{studentId}")
    Student findById(Integer studentId);

    @Delete("delete from student where studentId = #{studentId}")
    int deleteById(Integer studentId);

    /**
     *更新所有学生信息
     * @param student 传递一个对象
     * @return 受影响的记录条数
     */
    @Update("update student set studentName = #{studentName},grade = #{grade},major = #{major},clazz = #{clazz}," +
            "institute = #{institute},tel = #{tel},email = #{email},pwd = #{pwd},cardId = #{cardId},sex = #{sex},role = #{role} " +
            "where studentId = #{studentId}")
    int update(Student student);

    @Update("update student set studentName = #{studentName},grade = #{grade},major = #{major},clazz = #{clazz}," +
            "institute = #{institute},tel = #{tel},email = #{email},cardId = #{cardId},sex = #{sex},role = #{role} " +
            "where studentId = #{studentId}")
    int updateWithoutPassword(Student student);

    /**
     * 更新密码
     * @param student 传递参数
     * @return 受影响的记录条数
     */
    @Update("update student set pwd = #{pwd} where studentId = #{studentId}")
    int updatePwd(Student student);


    @Options(useGeneratedKeys = true,keyProperty = "studentId")
    @Insert("insert into student(studentName,grade,major,clazz,institute,tel,email,pwd,cardId,sex,role) values " +
            "(#{studentName},#{grade},#{major},#{clazz},#{institute},#{tel},#{email},#{pwd},#{cardId},#{sex},#{role})")
    int add(Student student);

    /**
     * 按身份证号查询（仅运维/排查；登录身份请以主键为准）。
     */
    @Select("SELECT * FROM student WHERE cardId = #{cardId} LIMIT 1")
    Student findByCardId(@Param("cardId") String cardId);

    @Select("SELECT COUNT(*) FROM student WHERE cardId = #{cid} AND cardId IS NOT NULL AND cardId <> ''")
    int countByCardId(@Param("cid") String cid);

    @Select("SELECT COUNT(*) FROM student WHERE cardId = #{cid} AND cardId IS NOT NULL AND cardId <> '' "
            + "AND studentId <> #{studentId}")
    int countByCardIdExcludingStudent(@Param("cid") String cid, @Param("studentId") int studentId);
}
