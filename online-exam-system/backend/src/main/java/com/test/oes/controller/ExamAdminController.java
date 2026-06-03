package com.test.oes.controller;

import com.test.oes.entity.Admin;
import com.test.oes.entity.ApiResult;
import com.test.oes.mapper.AdminMapper;
import com.test.oes.security.CurrentUserService;
import com.test.oes.service.ExamRevokeService;
import com.test.oes.util.ApiResultHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
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
    private final CurrentUserService currentUserService;

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/admin/exam/{examCode}/revoke")
    public ApiResult<Void> revokeExam(@PathVariable Integer examCode,
                                      @RequestBody Map<String, String> body) {
        Admin admin = adminMapper.findById(currentUserService.requireCurrentUser().getUserId());
        if (admin == null) {
            return ApiResultHandler.buildApiResult(403, "仅管理员账号可执行撤销", null);
        }
        String reason = body == null ? null : body.get("reason");
        examRevokeService.revokeExam(examCode, reason, admin);
        return ApiResultHandler.buildApiResult(200, "已撤销本场考试并记录审计", null);
    }
}
