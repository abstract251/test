package com.test.oes.service.impl;

import com.test.oes.entity.Admin;
import com.test.oes.entity.Student;
import com.test.oes.entity.Teacher;
import com.test.oes.mapper.LoginMapper;
import com.test.oes.service.LoginService;
import org.springframework.stereotype.Service;

@Service
public class LoginServiceImpl implements LoginService {

    private final LoginMapper loginMapper;

    public LoginServiceImpl(LoginMapper loginMapper) {
        this.loginMapper = loginMapper;
    }

    @Override
    public Admin adminLogin(Integer username, String password) {
        return loginMapper.adminLogin(username, password);
    }

    @Override
    public Teacher teacherLogin(Integer username, String password) {
        return loginMapper.teacherLogin(username, password);
    }

    @Override
    public Student studentLogin(Integer username, String password) {
        return loginMapper.studentLogin(username, password);
    }
}
