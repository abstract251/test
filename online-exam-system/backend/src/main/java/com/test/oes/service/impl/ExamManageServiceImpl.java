package com.test.oes.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.test.oes.entity.ExamManage;
import com.test.oes.exception.ExamBusinessException;
import com.test.oes.mapper.ExamManageMapper;
import com.test.oes.service.ExamManageService;
import com.test.oes.service.PaperService;
import com.test.oes.service.exam.ExamTimeHelper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ExamManageServiceImpl implements ExamManageService {

    private final ExamManageMapper examManageMapper;
    private final PaperService paperService;
    private final ExamTimeHelper examTimeHelper;

    private void setMaxScore(List<ExamManage> examManageList) {
        for (ExamManage examManage : examManageList) {
            if (examManage.getPaperId() != null) {
                examManage.setTotalScore(paperService.getMaxScore(examManage.getPaperId()));
            }
            enrichForApi(examManage);
        }
    }

    private void enrichForApi(ExamManage e) {
        if (e == null) {
            return;
        }
        if (e.getExamStartAt() == null) {
            LocalDateTime parsed = examTimeHelper.parseExamDateField(e.getExamDate());
            if (parsed != null) {
                e.setExamStartAt(parsed);
            }
        }
        if (e.getExamStartAt() != null) {
            e.setExamDate(examTimeHelper.formatExamDateDisplay(e.getExamStartAt()));
        }
    }

    @Override
    public List<ExamManage> findAll() {
        Page<ExamManage> examManage = new Page<>(0, 9999);
        List<ExamManage> examManageList = examManageMapper.findAll(examManage).getRecords();
        setMaxScore(examManageList);
        return examManageList;
    }

    @Override
    public IPage<ExamManage> findAll(Page<ExamManage> page) {
        IPage<ExamManage> iPage = examManageMapper.findAll(page);
        setMaxScore(iPage.getRecords());
        return iPage;
    }

    @Override
    public ExamManage findById(Integer examCode) {
        ExamManage examManage = examManageMapper.findById(examCode);
        if (examManage == null) {
            return null;
        }
        if (examManage.getPaperId() != null) {
            examManage.setTotalScore(paperService.getMaxScore(examManage.getPaperId()));
        }
        enrichForApi(examManage);
        return examManage;
    }

    @Override
    public int delete(Integer examCode) {
        ExamManage examManage = examManageMapper.findById(examCode);
        if (examManage == null) {
            return 0;
        }
        if (examTimeHelper.isRevoked(examManage)) {
            throw new ExamBusinessException(400, "考试已撤销，不能按原方式删除，请保留审计记录");
        }
        LocalDateTime now = examTimeHelper.nowShanghai();
        if (examTimeHelper.isPaperLocked(examManage, now)) {
            throw new ExamBusinessException(400, "考试已进入冻结窗口或相关时间约束已生效，不能删除");
        }
        paperService.deleteByPaperId(examManage.getPaperId());
        return examManageMapper.delete(examCode);
    }

    @Override
    public int update(ExamManage incoming) {
        if (incoming == null || incoming.getExamCode() == null) {
            return 0;
        }
        ExamManage cur = examManageMapper.findById(incoming.getExamCode());
        if (cur == null) {
            return 0;
        }
        if (examTimeHelper.isRevoked(cur)) {
            throw new ExamBusinessException(400, "考试已撤销，不能修改");
        }
        LocalDateTime now = examTimeHelper.nowShanghai();
        if (examTimeHelper.isPaperLocked(cur, now)) {
            int oldT = cur.getTotalTime() == null ? 0 : cur.getTotalTime();
            int newT = incoming.getTotalTime() == null ? oldT : incoming.getTotalTime();
            if (newT < oldT) {
                throw new ExamBusinessException(400, "冻结后考试时长只能延长，不能缩短");
            }
            return examManageMapper.updateWhitelist(incoming.getExamCode(), incoming.getDescription(), incoming.getTips(), newT);
        }
        LocalDateTime fallback = cur.getExamStartAt() != null
                ? cur.getExamStartAt()
                : examTimeHelper.parseExamDateField(cur.getExamDate());
        LocalDateTime start = examTimeHelper.resolveExamStartFromPayload(incoming, fallback);
        if (start == null) {
            start = examTimeHelper.nowShanghai();
        }
        incoming.setExamStartAt(start);
        incoming.setExamDate(examTimeHelper.formatExamDateDisplay(start));
        incoming.setPaperFrozenAt(cur.getPaperFrozenAt());
        incoming.setRevokedAt(cur.getRevokedAt());
        incoming.setRevokeReason(cur.getRevokeReason());
        Integer paperId = incoming.getPaperId() != null ? incoming.getPaperId() : cur.getPaperId();
        incoming.setPaperId(paperId);
        if (paperId != null) {
            incoming.setTotalScore(paperService.getMaxScore(paperId));
        }
        return examManageMapper.update(incoming);
    }

    @Override
    public int add(ExamManage exammanage) {
        if (exammanage.getTotalTime() == null) {
            exammanage.setTotalTime(90);
        }
        LocalDateTime start = examTimeHelper.resolveExamStartFromPayload(exammanage, examTimeHelper.nowShanghai());
        exammanage.setExamStartAt(start);
        exammanage.setExamDate(examTimeHelper.formatExamDateDisplay(start));
        exammanage.setPaperFrozenAt(null);
        exammanage.setRevokedAt(null);
        exammanage.setRevokeReason(null);
        if (exammanage.getPaperId() != null) {
            exammanage.setTotalScore(paperService.getMaxScore(exammanage.getPaperId()));
        }
        return examManageMapper.add(exammanage);
    }

    @Override
    public ExamManage findOnlyPaperId() {
        return examManageMapper.findOnlyPaperId();
    }
}
