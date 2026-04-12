package com.test.oes.controller;

import com.test.oes.entity.ApiResult;
import com.test.oes.entity.Student;
import com.test.oes.exception.ExamBusinessException;
import com.test.oes.mapper.StudentMapper;
import com.test.oes.service.StudentExamSessionService;
import com.test.oes.util.ApiResultHandler;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequiredArgsConstructor
public class StudentExamController {

    private final StudentExamSessionService studentExamSessionService;
    private final StudentMapper studentMapper;

    @PostMapping("/student/exam/{examCode}/attempt/start")
    public ApiResult<Map<String, Object>> start(@PathVariable Integer examCode, HttpServletRequest request) {
        Student student = requireStudent(request);
        Map<String, Object> data = studentExamSessionService.startOrResumeAttempt(examCode, student);
        return ApiResultHandler.buildApiResult(200, "ok", data);
    }

    @PutMapping("/student/exam/{examCode}/attempt/answers")
    public ApiResult<Void> saveAnswers(@PathVariable Integer examCode,
                                       @RequestBody Map<String, Object> body,
                                       HttpServletRequest request) {
        Student student = requireStudent(request);
        @SuppressWarnings("unchecked")
        Map<String, String> answers = (Map<String, String>) body.get("answers");
        studentExamSessionService.saveAnswers(examCode, student, answers == null ? Map.of() : answers);
        return ApiResultHandler.buildApiResult(200, "已保存", null);
    }

    @PostMapping("/student/exam/{examCode}/attempt/submit")
    public ApiResult<Map<String, Object>> submit(@PathVariable Integer examCode, HttpServletRequest request) {
        Student student = requireStudent(request);
        Map<String, Object> data = studentExamSessionService.submitAttempt(examCode, student);
        return ApiResultHandler.buildApiResult(200, "交卷成功", data);
    }

    private Student requireStudent(HttpServletRequest request) {
        assertStudentRole(request);
        String token = readCookie(request, "rb_token");
        if (token == null || token.isBlank()) {
            throw new ExamBusinessException(401, "未登录");
        }
        int studentPk;
        try {
            studentPk = Integer.parseInt(token.trim());
        } catch (NumberFormatException e) {
            throw new ExamBusinessException(401, "登录信息已失效，请重新登录");
        }
        Student s = studentMapper.findById(studentPk);
        if (s == null) {
            throw new ExamBusinessException(401, "未找到学生账号");
        }
        return s;
    }

    private static void assertStudentRole(HttpServletRequest request) {
        String role = readCookie(request, "rb_role");
        if (!"2".equals(role)) {
            throw new ExamBusinessException(403, "仅学生可访问");
        }
    }

    private static String readCookie(HttpServletRequest request, String name) {
        Cookie[] cookies = request.getCookies();
        if (cookies == null) {
            return null;
        }
        for (Cookie c : cookies) {
            if (name.equals(c.getName())) {
                return c.getValue();
            }
        }
        return null;
    }
}
