package com.test.oes.service.impl;

import com.test.oes.entity.Admin;
import com.test.oes.entity.Teacher;
import com.test.oes.mapper.AdminMapper;
import com.test.oes.mapper.TeacherMapper;
import com.test.oes.security.PasswordService;
import com.test.oes.service.AdminService;
import com.test.oes.service.identity.CardIdUniquenessValidator;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AdminServiceImpl implements AdminService {

    private final AdminMapper adminMapper;
    private final TeacherMapper teacherMapper;
    private final CardIdUniquenessValidator cardIdUniquenessValidator;
    private final PasswordService passwordService;

    public AdminServiceImpl(AdminMapper adminMapper, TeacherMapper teacherMapper,
                            CardIdUniquenessValidator cardIdUniquenessValidator,
                            PasswordService passwordService) {
        this.adminMapper = adminMapper;
        this.teacherMapper = teacherMapper;
        this.cardIdUniquenessValidator = cardIdUniquenessValidator;
        this.passwordService = passwordService;
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
        // 如果密码为空或者空字符串，则不更新密码
        if (admin.getPwd() == null || admin.getPwd().isBlank()) {
            return adminMapper.updateWithoutPassword(admin);
        }
        // 否则，加密密码后更新
        admin.setPwd(passwordService.encodeIfNeeded(admin.getPwd()));
        return adminMapper.update(admin);
    }

    @Override
    public int add(Admin admin) {
        cardIdUniquenessValidator.validateNewAdmin(admin);
        admin.setPwd(passwordService.encodeIfNeeded(admin.getPwd()));
        return adminMapper.add(admin);
    }

    @Override
    public Object resetPsw(Integer adminId, String newPsw, String oldPsw) {
        Admin admin = findById(adminId);
        if(admin != null && passwordService.matches(oldPsw, admin.getPwd())) {
            admin.setPwd(passwordService.encode(newPsw));
            update(admin);
            return true;

        }else if(admin == null){
            Teacher teacher = teacherMapper.findById(adminId);
            if(teacher != null && passwordService.matches(oldPsw, teacher.getPwd())) {
                teacher.setPwd(passwordService.encode(newPsw));
                teacherMapper.update(teacher);
                return true;

            }
        }
        return "原密码错误";
    }
}
