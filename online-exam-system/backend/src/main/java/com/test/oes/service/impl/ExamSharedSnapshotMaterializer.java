package com.test.oes.service.impl;

import com.test.oes.entity.*;
import com.test.oes.exception.ExamBusinessException;
import com.test.oes.mapper.*;
import com.test.oes.service.exam.ExamTimeHelper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 本场快照落库（独立 Bean，避免 {@code ExamSnapshotServiceImpl} 自调用导致 {@code @Transactional} 失效）。
 */
@Service
@RequiredArgsConstructor
public class ExamSharedSnapshotMaterializer {

    private final ExamManageMapper examManageMapper;
    private final PaperMapper paperMapper;
    private final ExamSharedSnapshotMapper examSharedSnapshotMapper;
    private final ExamSharedSnapshotItemMapper examSharedSnapshotItemMapper;
    private final ExamTimeHelper examTimeHelper;

    private final ConcurrentHashMap<Integer, Object> snapshotLocks = new ConcurrentHashMap<>();

    /**
     * 过冻结窗口且尚无快照时，从 {@code paper_manage} 复制并更新 {@code paper_frozen_at}。
     */
    @Transactional(rollbackFor = Exception.class)
    public void materializeIfAbsent(Integer examCode) {
        ExamManage exam = examManageMapper.findById(examCode);
        if (exam == null) {
            throw new ExamBusinessException(404, "考试不存在");
        }
        if (examTimeHelper.isRevoked(exam)) {
            return;
        }
        LocalDateTime now = examTimeHelper.nowShanghai();
        if (examTimeHelper.shouldNotMaterializeSnapshot(exam, now)) {
            return;
        }
        Object lock = snapshotLocks.computeIfAbsent(examCode, k -> new Object());
        synchronized (lock) {
            if (examSharedSnapshotMapper.countByExamCode(examCode) > 0) {
                return;
            }
            exam = examManageMapper.findById(examCode);
            if (exam == null || examTimeHelper.isRevoked(exam)) {
                return;
            }
            if (examTimeHelper.shouldNotMaterializeSnapshot(exam, examTimeHelper.nowShanghai())) {
                return;
            }
            Integer paperId = exam.getPaperId();
            if (paperId == null) {
                throw new ExamBusinessException(400, "考试未关联试卷，无法生成本场快照");
            }
            List<PaperManage> rows = new ArrayList<>(paperMapper.findById(paperId));
            rows.sort(Comparator.comparing(PaperManage::getQuestionType).thenComparing(PaperManage::getQuestionId));
            LocalDateTime created = examTimeHelper.nowShanghai();
            ExamSharedSnapshot head = new ExamSharedSnapshot();
            head.setExamCode(examCode);
            head.setPaperId(paperId);
            head.setCreatedAt(created);
            examSharedSnapshotMapper.insert(head);
            int order = 0;
            for (PaperManage row : rows) {
                ExamSharedSnapshotItem item = new ExamSharedSnapshotItem();
                item.setExamCode(examCode);
                item.setQuestionType(row.getQuestionType());
                item.setQuestionId(row.getQuestionId());
                item.setDisplayOrder(order++);
                examSharedSnapshotItemMapper.insert(item);
            }
            examManageMapper.updatePaperFrozenAt(examCode, created);
        }
    }
}
