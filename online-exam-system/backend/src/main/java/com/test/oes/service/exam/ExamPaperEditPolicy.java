package com.test.oes.service.exam;

import com.test.oes.entity.ExamManage;
import com.test.oes.exception.ExamBusinessException;
import com.test.oes.mapper.ExamManageMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 试卷结构类操作门禁：关联考试进入冻结窗口或已撤销后禁止改题。
 */
@Component
@RequiredArgsConstructor
public class ExamPaperEditPolicy {

    private final ExamManageMapper examManageMapper;
    private final ExamTimeHelper examTimeHelper;

    public void assertPaperEditable(Integer paperId) {
        if (paperId == null) {
            return;
        }
        List<ExamManage> exams = examManageMapper.findByPaperId(paperId);
        if (exams == null || exams.isEmpty()) {
            return;
        }
        LocalDateTime now = examTimeHelper.nowShanghai();
        for (ExamManage e : exams) {
            if (examTimeHelper.isRevoked(e)) {
                throw new ExamBusinessException(400, "关联考试已撤销，不能修改试卷内容");
            }
            if (examTimeHelper.isPaperLocked(e, now)) {
                throw new ExamBusinessException(400, "考试试卷已冻结，不能再调整题目或分值");
            }
        }
    }
}
