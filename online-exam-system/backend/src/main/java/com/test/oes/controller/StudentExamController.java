package com.test.oes.controller;

import com.test.oes.entity.ApiResult;
import com.test.oes.entity.Student;
import com.test.oes.exception.ExamBusinessException;
import com.test.oes.mapper.StudentMapper;
import com.test.oes.security.AccountRole;
import com.test.oes.security.CurrentUserService;
import com.test.oes.service.StudentExamQueryService;
import com.test.oes.service.StudentExamSessionService;
import com.test.oes.util.ApiResultHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequiredArgsConstructor
public class StudentExamController {

    private final StudentExamQueryService studentExamQueryService;
    private final StudentExamSessionService studentExamSessionService;
    private final StudentMapper studentMapper;
    private final CurrentUserService currentUserService;

    @GetMapping("/student/exams")
    public ApiResult<Map<String, Object>> exams() {
        Student student = requireStudent();
        return ApiResultHandler.buildApiResult(200, "ok", studentExamQueryService.getStudentExamList(student));
    }

    @GetMapping("/student/exam/{examCode}")
    public ApiResult<Map<String, Object>> examDetail(@PathVariable Integer examCode) {
        Student student = requireStudent();
        return ApiResultHandler.buildApiResult(200, "ok", studentExamQueryService.getStudentExamDetail(examCode, student));
    }

    @PostMapping("/student/exam/{examCode}/attempt/start")
    public ApiResult<Map<String, Object>> start(@PathVariable Integer examCode) {
        Student student = requireStudent();
        Map<String, Object> data = studentExamSessionService.startOrResumeAttempt(examCode, student);
        return ApiResultHandler.buildApiResult(200, "ok", data);
    }

    @PutMapping("/student/exam/{examCode}/attempt/answers")
    public ApiResult<Void> saveAnswers(@PathVariable Integer examCode,
                                       @RequestBody Map<String, Object> body) {
        Student student = requireStudent();
        @SuppressWarnings("unchecked")
        Map<String, String> answers = (Map<String, String>) body.get("answers");
        studentExamSessionService.saveAnswers(examCode, student, answers == null ? Map.of() : answers);
        return ApiResultHandler.buildApiResult(200, "已保存", null);
    }

    @PostMapping("/student/exam/{examCode}/attempt/submit")
    public ApiResult<Map<String, Object>> submit(@PathVariable Integer examCode) {
        Student student = requireStudent();
        Map<String, Object> data = studentExamSessionService.submitAttempt(examCode, student);
        return ApiResultHandler.buildApiResult(200, "交卷成功", data);
    }

    private Student requireStudent() {
        if (currentUserService.requireCurrentUser().getAccountRole() != AccountRole.STUDENT) {
            throw new ExamBusinessException(403, "仅学生可访问");
        }
        Student s = studentMapper.findById(currentUserService.requireCurrentUser().getUserId());
        if (s == null) {
            throw new ExamBusinessException(401, "未找到学生账号");
        }
        return s;
    }
}
