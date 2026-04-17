package com.test.oes.service.impl;

import com.test.oes.entity.ExamManage;
import com.test.oes.entity.ExamSharedSnapshot;
import com.test.oes.entity.ExamSharedSnapshotItem;
import com.test.oes.entity.PaperManage;
import com.test.oes.exception.ExamBusinessException;
import com.test.oes.mapper.ExamManageMapper;
import com.test.oes.mapper.ExamSharedSnapshotItemMapper;
import com.test.oes.mapper.ExamSharedSnapshotMapper;
import com.test.oes.mapper.PaperMapper;
import com.test.oes.service.exam.ExamTimeHelper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ExamSharedSnapshotTxWriter {

    private final ExamManageMapper examManageMapper;
    private final PaperMapper paperMapper;
    private final ExamSharedSnapshotMapper examSharedSnapshotMapper;
    private final ExamSharedSnapshotItemMapper examSharedSnapshotItemMapper;
    private final ExamTimeHelper examTimeHelper;

    @Transactional(propagation = Propagation.REQUIRES_NEW, rollbackFor = Exception.class)
    public void materializeIfStillAbsent(Integer examCode) {
        ExamManage exam = examManageMapper.findById(examCode);
        if (exam == null || examTimeHelper.isRevoked(exam)) {
            return;
        }
        LocalDateTime now = examTimeHelper.nowShanghai();
        if (examTimeHelper.shouldNotMaterializeSnapshot(exam, now)) {
            return;
        }
        if (examSharedSnapshotMapper.countByExamCode(examCode) > 0) {
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

        int inserted = examSharedSnapshotMapper.insertIgnore(head);
        if (inserted == 0) {
            return;
        }

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
