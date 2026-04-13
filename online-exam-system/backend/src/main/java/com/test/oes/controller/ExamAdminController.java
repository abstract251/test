package com.test.oes.controller;

import com.test.oes.entity.Admin;
import com.test.oes.entity.ApiResult;
import com.test.oes.mapper.AdminMapper;
import com.test.oes.service.ExamRevokeService;
import com.test.oes.util.ApiResultHandler;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * 超级管理员：考试应急操作（评审稿 §7）。
 */
@RestController
@RequiredArgsConstructor
public class ExamAdminController {

    private final ExamRevokeService examRevokeService;
    private final AdminMapper adminMapper;

    @PostMapping("/admin/exam/{examCode}/revoke")
    public ApiResult<Void> revokeExam(@PathVariable Integer examCode,
                                      @RequestBody Map<String, String> body,
                                      HttpServletRequest request) {
        String token = readCookie(request, "rb_token");
        if (token == null || token.isBlank()) {
            return ApiResultHandler.buildApiResult(401, "未登录", null);
        }
        int adminId;
        try {
            adminId = Integer.parseInt(token.trim());
        } catch (NumberFormatException e) {
            return ApiResultHandler.buildApiResult(401, "登录信息已失效，请重新登录", null);
        }
        Admin admin = adminMapper.findById(adminId);
        if (admin == null) {
            return ApiResultHandler.buildApiResult(403, "仅管理员账号可执行撤销", null);
        }
        String reason = body == null ? null : body.get("reason");
        examRevokeService.revokeExam(examCode, reason, admin);
        return ApiResultHandler.buildApiResult(200, "已撤销本场考试并记录审计", null);
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
