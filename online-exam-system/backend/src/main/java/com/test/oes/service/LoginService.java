package com.test.oes.service;

import com.test.oes.entity.Admin;
import com.test.oes.entity.Student;
import com.test.oes.entity.Teacher;

public interface LoginService {

    Admin adminLogin(Integer username, String password);

    Teacher teacherLogin(Integer username, String password);

    Student studentLogin(Integer username, String password);
}