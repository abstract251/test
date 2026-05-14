package com.test.oes.service;

import com.test.oes.entity.Admin;

public interface ExamRevokeService {

    /**
     * 超级管理员撤销考试并写审计（评审稿 §7.1）。
     */
    void revokeExam(Integer examCode, String reason, Admin operator);
}
