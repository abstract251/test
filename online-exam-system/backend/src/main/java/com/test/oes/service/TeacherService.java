package com.test.oes.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.test.oes.entity.Teacher;

import java.util.List;

public interface TeacherService {

    IPage<Teacher> findAll(Page<Teacher> page, String teacherId, String teacherName,
                           String institute, String type, String tel, String email);

    List<Teacher> findAll();

    Teacher findById(Integer teacherId);

    int deleteById(Integer teacherId);

    int update(Teacher teacher);

    int add(Teacher teacher);
}

