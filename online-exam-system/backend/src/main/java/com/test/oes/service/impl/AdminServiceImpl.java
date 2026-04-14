package com.test.oes.service.impl;

import com.test.oes.entity.Admin;
import com.test.oes.entity.Teacher;
import com.test.oes.mapper.AdminMapper;
import com.test.oes.mapper.TeacherMapper;
import com.test.oes.service.AdminService;
import com.test.oes.service.identity.CardIdUniquenessValidator;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AdminServiceImpl implements AdminService {

    private final AdminMapper adminMapper;
    private final TeacherMapper teacherMapper;
    private final CardIdUniquenessValidator cardIdUniquenessValidator;

    public AdminServiceImpl(AdminMapper adminMapper, TeacherMapper teacherMapper,
                            CardIdUniquenessValidator cardIdUniquenessValidator) {
        this.adminMapper = adminMapper;
        this.teacherMapper = teacherMapper;
        this.cardIdUniquenessValidator = cardIdUniquenessValidator;
    }

    @Override
    public List<Admin> findAll() {
        return adminMapper.findAll();
    }

    @Override
    public Admin findById(Integer adminId) {
        return adminMapper.findById(adminId);
    }

    @Override
    public int deleteById(int adminId) {
        return adminMapper.deleteById(adminId);
    }

    @Override
    public int update(Admin admin) {
        cardIdUniquenessValidator.validateAdminUpdate(admin);
        return adminMapper.update(admin);
    }

    @Override
    public int add(Admin admin) {
        cardIdUniquenessValidator.validateNewAdmin(admin);
        return adminMapper.add(admin);
    }

    @Override
    public Object resetPsw(Integer adminId, String newPsw, String oldPsw) {
        Admin admin = findById(adminId);
        if(admin != null && admin.getPwd().equals(oldPsw)) {
            admin.setPwd(String.valueOf(newPsw));
            update(admin);
            return true;

        }else if(admin == null){
            Teacher teacher = teacherMapper.findById(adminId);
            if(teacher != null && teacher.getPwd().equals(oldPsw)) {
                teacher.setPwd(String.valueOf(newPsw));
                teacherMapper.update(teacher);
                return true;

            }
        }
        return "原密码错误";
    }
}
