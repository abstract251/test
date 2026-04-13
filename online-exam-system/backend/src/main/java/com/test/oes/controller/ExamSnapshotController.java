package com.test.oes.controller;

import com.test.oes.entity.ApiResult;
import com.test.oes.exception.ExamBusinessException;
import com.test.oes.service.ExamSnapshotService;
import com.test.oes.util.ApiResultHandler;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * 拉取冻结后的本场共用题目快照（与 {@code /paper/{paperId}} 结构一致）。
 * 仅教师/管理员可直连；学生应通过 {@code POST /student/exam/{examCode}/attempt/start} 在开考后获取试题，防止考前泄题。
 */
@RestController
@RequiredArgsConstructor
public class ExamSnapshotController {

    private final ExamSnapshotService examSnapshotService;

    @GetMapping("/exam/{examCode}/frozen-paper")
    public ApiResult<Map<Integer, List<?>>> frozenPaper(@PathVariable Integer examCode, HttpServletRequest request) {
        assertNotStudentRole(request);
        Map<Integer, List<?>> data = examSnapshotService.buildFrozenPaperMap(examCode);
        return ApiResultHandler.buildApiResult(200, "查询成功", data);
    }

    private static void assertNotStudentRole(HttpServletRequest request) {
        if ("2".equals(readCookie(request, "rb_role"))) {
            throw new ExamBusinessException(403, "学生无法预览本场试题，请在开考后从答题页进入");
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
