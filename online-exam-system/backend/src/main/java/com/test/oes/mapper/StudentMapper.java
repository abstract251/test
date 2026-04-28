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
    IPage<Student> findAll(Page<Student> page, @Param("studentId") String studentId,
                           @Param("name") String name, @Param("grade") String grade,
                           @Param("tel") String tel,  @Param("institute") String institute,
                           @Param("major")String major, @Param("clazz") String clazz);

    Student findById(Integer studentId);

    int deleteById(Integer studentId);

    /**
     *更新所有学生信息
     * @param student 传递一个对象
     * @return 受影响的记录条数
     */
    int update(Student student);

    /**
     * 更新密码
     * @param student 传递参数
     * @return 受影响的记录条数
     */
    int updatePwd(Student student);


    int add(Student student);

    /**
     * 按身份证号查询（仅运维/排查；登录身份请以主键为准）。
     */
    Student findByCardId(@Param("cardId") String cardId);

    int countByCardId(@Param("cid") String cid);

    int countByCardIdExcludingStudent(@Param("cid") String cid, @Param("studentId") int studentId);
}
