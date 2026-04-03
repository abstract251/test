package com.test.oes.service;

import com.test.oes.entity.Admin;

import java.util.List;

public interface AdminService{

    List<Admin> findAll();

    Admin findById(Integer adminId);

    int deleteById(int adminId);

    int update(Admin admin);

    int add(Admin admin);

    Object resetPsw(Integer adminId, String newPsw, String oldPsw);
}