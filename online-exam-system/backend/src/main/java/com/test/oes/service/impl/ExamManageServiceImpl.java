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
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Objects;

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
        e.setPaperLocked(examTimeHelper.isPaperLocked(e, examTimeHelper.nowShanghai()));
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
            assertNoDisallowedChangesWhenPaperLocked(incoming, cur);
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

    /**
     * 冻结后仅允许改说明、考生提示、时长（只增不减）；若请求中其它字段与当前不一致则拒绝，避免误提示「成功」。
     */
    private void assertNoDisallowedChangesWhenPaperLocked(ExamManage incoming, ExamManage cur) {
        if (!strEq(incoming.getSource(), cur.getSource())) {
            throw new ExamBusinessException(400, "试卷已冻结，不能修改科目等基础信息，仅可调整说明、考生提示与考试时长");
        }
        if (!strEq(incoming.getType(), cur.getType())) {
            throw new ExamBusinessException(400, "试卷已冻结，不能修改考试类型");
        }
        if (!strEq(incoming.getGrade(), cur.getGrade())) {
            throw new ExamBusinessException(400, "试卷已冻结，不能修改年级范围");
        }
        if (!strEq(incoming.getTerm(), cur.getTerm())) {
            throw new ExamBusinessException(400, "试卷已冻结，不能修改学期");
        }
        if (!strEq(incoming.getMajor(), cur.getMajor())) {
            throw new ExamBusinessException(400, "试卷已冻结，不能修改专业范围");
        }
        if (!strEq(incoming.getInstitute(), cur.getInstitute())) {
            throw new ExamBusinessException(400, "试卷已冻结，不能修改学院范围");
        }
        if (!Objects.equals(incoming.getPaperId(), cur.getPaperId())) {
            throw new ExamBusinessException(400, "试卷已冻结，不能更换试卷");
        }
        if (incoming.getTotalScore() != null && cur.getTotalScore() != null
                && !incoming.getTotalScore().equals(cur.getTotalScore())) {
            throw new ExamBusinessException(400, "试卷已冻结，不能通过保存修改总分（总分随试卷自动计算）");
        }
        LocalDateTime curStart = cur.getExamStartAt() != null
                ? cur.getExamStartAt()
                : examTimeHelper.parseExamDateField(cur.getExamDate());
        LocalDateTime reqStart = examTimeHelper.resolveExamStartFromPayload(incoming, curStart);
        if (curStart != null && reqStart != null) {
            LocalDateTime a = curStart.truncatedTo(ChronoUnit.MINUTES);
            LocalDateTime b = reqStart.truncatedTo(ChronoUnit.MINUTES);
            if (!a.equals(b)) {
                throw new ExamBusinessException(400, "试卷已冻结，不能修改开考时间");
            }
        }
    }

    private static boolean strEq(String a, String b) {
        String x = a == null ? "" : a.trim();
        String y = b == null ? "" : b.trim();
        return x.equals(y);
    }
}
