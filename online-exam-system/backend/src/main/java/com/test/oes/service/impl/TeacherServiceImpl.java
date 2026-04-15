package com.test.oes.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.test.oes.entity.Teacher;
import com.test.oes.mapper.TeacherMapper;
import com.test.oes.service.TeacherService;
import com.test.oes.service.identity.CardIdUniquenessValidator;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TeacherServiceImpl implements TeacherService {
    private final TeacherMapper teacherMapper;
    private final CardIdUniquenessValidator cardIdUniquenessValidator;

    public TeacherServiceImpl(TeacherMapper teacherMapper, CardIdUniquenessValidator cardIdUniquenessValidator) {
        this.teacherMapper = teacherMapper;
        this.cardIdUniquenessValidator = cardIdUniquenessValidator;
    }

    @Override
    public IPage<Teacher> findAll(Page<Teacher> page, String teacherId, String teacherName,
                                  String institute, String type, String tel, String email) {
        teacherId = ("@".equals(teacherId) ? "" : teacherId);
        teacherName = ("@".equals(teacherName) ? "" : teacherName);
        institute = ("@".equals(institute) ? "" : institute);
        type = ("@".equals(type) ? "" : type);
        tel = ("@".equals(tel) ? "" : tel);
        email = ("@".equals(email) ? "" : email);
        return teacherMapper.findAll(page, teacherId, teacherName, institute, type, tel, email);
    }

    @Override
    public List<Teacher> findAll() {
        Page<Teacher> teacherPage = new Page<>(1, 9999);
        return teacherMapper.findAll(teacherPage, "", "", "", "", "", "").getRecords();
    }

    @Override
    public Teacher findById(Integer teacherId) {
        return teacherMapper.findById(teacherId);
    }

    @Override
    public int deleteById(Integer teacherId) {
        return teacherMapper.deleteById(teacherId);
    }

    @Override
    public int update(Teacher teacher) {
        cardIdUniquenessValidator.validateTeacherUpdate(teacher);
        return teacherMapper.update(teacher);
    }

    @Override
    public int add(Teacher teacher) {
        teacher.setRole("1");
        cardIdUniquenessValidator.validateNewTeacher(teacher);
        return teacherMapper.add(teacher);
    }
}
