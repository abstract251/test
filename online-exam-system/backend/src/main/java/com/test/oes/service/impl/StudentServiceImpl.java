package com.test.oes.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.test.oes.entity.Student;
import com.test.oes.mapper.StudentMapper;
import com.test.oes.security.PasswordService;
import com.test.oes.service.StudentService;
import com.test.oes.service.identity.CardIdUniquenessValidator;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class StudentServiceImpl implements StudentService {
    private final StudentMapper studentMapper;
    private final CardIdUniquenessValidator cardIdUniquenessValidator;
    private final PasswordService passwordService;

    public StudentServiceImpl(StudentMapper studentMapper, CardIdUniquenessValidator cardIdUniquenessValidator,
                              PasswordService passwordService) {
        this.studentMapper = studentMapper;
        this.cardIdUniquenessValidator = cardIdUniquenessValidator;
        this.passwordService = passwordService;
    }

    @Override
    @Transactional(readOnly = true)
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
    @Transactional(readOnly = true)
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
        student.setPwd(passwordService.encodeIfNeeded(student.getPwd()));
        return studentMapper.update(student);
    }

    @Override
    public int updatePwd(Student student) {
        student.setPwd(passwordService.encodeIfNeeded(student.getPwd()));
        return studentMapper.updatePwd(student);
    }

    @Override
    public int add(Student student) {
        cardIdUniquenessValidator.validateNewStudent(student);
        student.setPwd(passwordService.encodeIfNeeded(student.getPwd()));
        return studentMapper.add(student);
    }
}
