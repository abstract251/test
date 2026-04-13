package com.test.oes.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.test.oes.entity.Student;
import com.test.oes.mapper.StudentMapper;
import com.test.oes.service.StudentService;
import com.test.oes.service.identity.CardIdUniquenessValidator;
import org.springframework.stereotype.Service;

@Service
public class StudentServiceImpl implements StudentService {
    private final StudentMapper studentMapper;
    private final CardIdUniquenessValidator cardIdUniquenessValidator;

    public StudentServiceImpl(StudentMapper studentMapper, CardIdUniquenessValidator cardIdUniquenessValidator) {
        this.studentMapper = studentMapper;
        this.cardIdUniquenessValidator = cardIdUniquenessValidator;
    }

    @Override
    public IPage<Student> findAll(
            Page<Student> page, String studentId, String name, String grade,
            String tel, String institute, String major, String clazz) {
        studentId = ("@".equals(studentId) ? "" : studentId);
        name = ("@".equals(name) ? "" : name);
        grade = ("@".equals(grade) ? "" : grade);
        tel = ("@".equals(tel) ? "" : tel);
        institute = ("@".equals(institute) ? "" : institute);
        major = ("@".equals(major) ? "" : major);
        clazz = ("@".equals(clazz) ? "" : clazz);
        return studentMapper.findAll(page, studentId, name, grade, tel, institute, major, clazz);
    }

    @Override
    public Student findById(Integer studentId) {
        return studentMapper.findById(studentId);
    }

    @Override
    public int deleteById(Integer studentId) {
        return studentMapper.deleteById(studentId);
    }

    @Override
    public int update(Student student) {
        cardIdUniquenessValidator.validateStudentUpdate(student);
        return studentMapper.update(student);
    }

    @Override
    public int updatePwd(Student student) {
        return studentMapper.updatePwd(student);
    }

    @Override
    public int add(Student student) {
        cardIdUniquenessValidator.validateNewStudent(student);
        return studentMapper.add(student);
    }
}
