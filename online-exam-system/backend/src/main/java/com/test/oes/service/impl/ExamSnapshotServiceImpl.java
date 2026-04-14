package com.test.oes.service.impl;

import com.test.oes.entity.*;
import com.test.oes.exception.ExamBusinessException;
import com.test.oes.mapper.*;
import com.test.oes.service.ExamSnapshotService;
import com.test.oes.service.exam.ExamTimeHelper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
public class ExamSnapshotServiceImpl implements ExamSnapshotService {

    private final ExamManageMapper examManageMapper;
    private final ExamSharedSnapshotMapper examSharedSnapshotMapper;
    private final ExamSharedSnapshotItemMapper examSharedSnapshotItemMapper;
    private final MultiQuestionMapper multiQuestionMapper;
    private final FillQuestionMapper fillQuestionMapper;
    private final JudgeQuestionMapper judgeQuestionMapper;
    private final ExamTimeHelper examTimeHelper;
    private final ExamSharedSnapshotMaterializer snapshotMaterializer;

    @Override
    public boolean hasSharedSnapshot(Integer examCode) {
        return examSharedSnapshotMapper.countByExamCode(examCode) > 0;
    }

    @Override
    public void ensureSharedSnapshot(Integer examCode) {
        snapshotMaterializer.materializeIfAbsent(examCode);
    }

    @Override
    public Map<Integer, List<?>> buildFrozenPaperMap(Integer examCode) {
        ExamManage exam = examManageMapper.findById(examCode);
        if (exam == null) {
            throw new ExamBusinessException(404, "考试不存在");
        }
        if (examTimeHelper.isRevoked(exam)) {
            throw new ExamBusinessException(410, "本场考试已撤销");
        }
        LocalDateTime now = examTimeHelper.nowShanghai();
        if (examTimeHelper.shouldNotMaterializeSnapshot(exam, now)) {
            throw new ExamBusinessException(400, "尚未到达试卷冻结时刻，本场共用快照尚未就绪");
        }
        snapshotMaterializer.materializeIfAbsent(examCode);
        if (examSharedSnapshotMapper.countByExamCode(examCode) == 0) {
            throw new ExamBusinessException(500, "本场快照生成失败，请稍后重试");
        }
        List<ExamSharedSnapshotItem> items = examSharedSnapshotItemMapper.findByExamCode(examCode);
        List<MultiQuestion> multi = new ArrayList<>();
        List<FillQuestion> fill = new ArrayList<>();
        List<JudgeQuestion> judge = new ArrayList<>();
        for (ExamSharedSnapshotItem it : items) {
            switch (it.getQuestionType()) {
                case 1 -> {
                    MultiQuestion q = multiQuestionMapper.findByQuestionId(it.getQuestionId());
                    if (q == null) {
                        throw new ExamBusinessException(500, "快照题目缺失，请联系管理员（选择题）");
                    }
                    multi.add(q);
                }
                case 2 -> {
                    FillQuestion q = fillQuestionMapper.findByQuestionId(it.getQuestionId());
                    if (q == null) {
                        throw new ExamBusinessException(500, "快照题目缺失，请联系管理员（填空题）");
                    }
                    fill.add(q);
                }
                case 3 -> {
                    JudgeQuestion q = judgeQuestionMapper.findByQuestionId(it.getQuestionId());
                    if (q == null) {
                        throw new ExamBusinessException(500, "快照题目缺失，请联系管理员（判断题）");
                    }
                    judge.add(q);
                }
                default -> throw new ExamBusinessException(500, "未知题型");
            }
        }
        Map<Integer, List<?>> map = new HashMap<>(3);
        map.put(1, multi);
        map.put(2, fill);
        map.put(3, judge);
        return map;
    }
}
