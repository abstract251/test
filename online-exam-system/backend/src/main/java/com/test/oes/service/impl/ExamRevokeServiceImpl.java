package com.test.oes.service.impl;

import com.test.oes.async.AsyncEventEnvelope;
import com.test.oes.async.AsyncEventTypes;
import com.test.oes.async.AsyncRoutingKeys;
import com.test.oes.async.OutboxEventService;
import com.test.oes.async.payload.ExamRevokedPayload;
import com.test.oes.cache.ExamCacheFacade;
import com.test.oes.entity.Admin;
import com.test.oes.entity.ExamManage;
import com.test.oes.entity.ExamRevokeAudit;
import com.test.oes.exception.ExamBusinessException;
import com.test.oes.mapper.ExamManageMapper;
import com.test.oes.mapper.ExamRevokeAuditMapper;
import com.test.oes.service.ExamRevokeService;
import com.test.oes.service.exam.ExamTimeHelper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class ExamRevokeServiceImpl implements ExamRevokeService {

    private final ExamManageMapper examManageMapper;
    private final ExamRevokeAuditMapper examRevokeAuditMapper;
    private final ExamTimeHelper examTimeHelper;
    private final ExamCacheFacade examCacheFacade;
    private final OutboxEventService outboxEventService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void revokeExam(Integer examCode, String reason, Admin operator) {
        if (operator == null) {
            throw new ExamBusinessException(401, "未识别管理员身份");
        }
        if (operator.getRole() == null || !operator.getRole().equals("0")) {
            throw new ExamBusinessException(403, "仅超级管理员可撤销考试");
        }
        if (reason == null || reason.isBlank()) {
            throw new ExamBusinessException(400, "请填写撤销原因");
        }
        ExamManage exam = examManageMapper.findById(examCode);
        if (exam == null) {
            throw new ExamBusinessException(404, "考试不存在");
        }
        if (exam.getRevokedAt() != null) {
            throw new ExamBusinessException(400, "该考试已撤销");
        }
        LocalDateTime now = examTimeHelper.nowShanghai();
        String trimmed = reason.trim();
        int rows = examManageMapper.markRevoked(examCode, now, trimmed);
        if (rows == 0) {
            throw new ExamBusinessException(500, "撤销状态更新失败");
        }

        ExamRevokeAudit audit = new ExamRevokeAudit();
        audit.setExamCode(examCode);
        audit.setAdminId(operator.getAdminId());
        audit.setAdminName(operator.getAdminName());
        audit.setReason(trimmed);
        audit.setDetail("subject=" + exam.getSource() + ", paperId=" + exam.getPaperId());
        audit.setCreatedAt(now);
        examRevokeAuditMapper.insert(audit);

        examCacheFacade.evictExamMeta(examCode);
        examCacheFacade.evictSnapshotCaches(examCode);
        examCacheFacade.evictScoreStatistics(examCode);
        examCacheFacade.bumpExamMetaVersion(examCode);
        examCacheFacade.bumpSnapshotVersion(examCode);
        examCacheFacade.bumpScoreStatisticsVersion(examCode);
        examCacheFacade.bumpScopeExamVersion();
        examCacheFacade.markScoreProjectionDirty(examCode);

        ExamRevokedPayload payload = new ExamRevokedPayload();
        payload.setExamCode(examCode);
        payload.setRevokedAt(now);
        payload.setAdminId(operator.getAdminId());
        payload.setReason(trimmed);
        payload.setPaperId(exam.getPaperId());

        AsyncEventEnvelope<ExamRevokedPayload> envelope = new AsyncEventEnvelope<>();
        envelope.setEventType(AsyncEventTypes.EXAM_REVOKED);
        envelope.setAggregateType("exam");
        envelope.setAggregateId(String.valueOf(examCode));
        envelope.setOccurredAt(now);
        envelope.setPayloadVersion(1);
        envelope.setPayload(payload);

        outboxEventService.append(
                AsyncEventTypes.EXAM_REVOKED,
                "exam",
                String.valueOf(examCode),
                AsyncRoutingKeys.EXAM_REVOKED,
                envelope
        );
    }
}
