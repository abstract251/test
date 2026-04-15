package com.test.oes.service.identity;

import com.test.oes.entity.Admin;
import com.test.oes.entity.Student;
import com.test.oes.entity.Teacher;
import com.test.oes.exception.ExamBusinessException;
import com.test.oes.mapper.AdminMapper;
import com.test.oes.mapper.StudentMapper;
import com.test.oes.mapper.TeacherMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * 身份证号在 student / teacher / admin 三表间互斥，避免登录 Cookie 误用 cardId 时的重复问题。
 */
@Component
@RequiredArgsConstructor
public class CardIdUniquenessValidator {

    private final StudentMapper studentMapper;
    private final TeacherMapper teacherMapper;
    private final AdminMapper adminMapper;

    public void validateNewStudent(Student student) {
        String cid = normalize(student.getCardId());
        requireNonBlank(cid);
        assertUniqueAmongAllRoles(cid, null, null, null);
    }

    public void validateStudentUpdate(Student student) {
        String cid = normalize(student.getCardId());
        requireNonBlank(cid);
        int sid = requirePositiveId(student.getStudentId(), "学生编号");
        assertUniqueAmongAllRoles(cid, sid, null, null);
    }

    public void validateNewTeacher(Teacher teacher) {
        String cid = normalize(teacher.getCardId());
        requireNonBlank(cid);
        assertUniqueAmongAllRoles(cid, null, null, null);
    }

    public void validateTeacherUpdate(Teacher teacher) {
        String cid = normalize(teacher.getCardId());
        requireNonBlank(cid);
        int tid = parseTeacherId(teacher.getTeacherId(), "教师编号");
        assertUniqueAmongAllRoles(cid, null, tid, null);
    }

    public void validateNewAdmin(Admin admin) {
        String cid = normalize(admin.getCardId());
        requireNonBlank(cid);
        assertUniqueAmongAllRoles(cid, null, null, null);
    }

    public void validateAdminUpdate(Admin admin) {
        String cid = normalize(admin.getCardId());
        requireNonBlank(cid);
        if (admin.getAdminId() == null) {
            throw new ExamBusinessException(400, "管理员编号不能为空");
        }
        assertUniqueAmongAllRoles(cid, null, null, admin.getAdminId());
    }

    private void assertUniqueAmongAllRoles(String cid, Integer excludeStudentId, Integer excludeTeacherId, Integer excludeAdminId) {
        int s = excludeStudentId == null
                ? studentMapper.countByCardId(cid)
                : studentMapper.countByCardIdExcludingStudent(cid, excludeStudentId);
        if (s > 0) {
            throw new ExamBusinessException(400, "该身份证号已存在系统中，请勿重复添加");
        }
        int t = excludeTeacherId == null
                ? teacherMapper.countByCardId(cid)
                : teacherMapper.countByCardIdExcludingTeacher(cid, excludeTeacherId);
        if (t > 0) {
            throw new ExamBusinessException(400, "该身份证号已存在系统中，请勿重复添加");
        }
        int a = excludeAdminId == null
                ? adminMapper.countByCardId(cid)
                : adminMapper.countByCardIdExcludingAdmin(cid, excludeAdminId);
        if (a > 0) {
            throw new ExamBusinessException(400, "该身份证号已存在系统中，请勿重复添加");
        }
    }

    private static String normalize(String raw) {
        return raw == null ? "" : raw.trim();
    }

    private static void requireNonBlank(String cid) {
        if (cid.isEmpty()) {
            throw new ExamBusinessException(400, "身份证号不能为空");
        }
    }

    private static int requirePositiveId(int id, String label) {
        if (id <= 0) {
            throw new ExamBusinessException(400, label + "无效");
        }
        return id;
    }

    private static int parseTeacherId(String raw, String label) {
        try {
            return Integer.parseInt(raw.trim());
        } catch (Exception e) {
            throw new ExamBusinessException(400, label + "无效");
        }
    }
}
