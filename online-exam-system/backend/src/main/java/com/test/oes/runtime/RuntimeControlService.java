package com.test.oes.runtime;

import com.test.oes.config.DbRouteContext;
import com.test.oes.exception.ExamBusinessException;
import com.test.oes.mapper.RuntimeFeatureToggleAuditMapper;
import com.test.oes.mapper.RuntimeFeatureToggleMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RuntimeControlService {

    private final RuntimeFeatureToggleMapper toggleMapper;
    private final RuntimeFeatureToggleAuditMapper auditMapper;
    private final RuntimeToggleManager runtimeToggleManager;
    private final RuntimeControlMetrics runtimeControlMetrics;

    @Transactional
    public RuntimeToggleSnapshot refreshSnapshot() {
        try {
            DbRouteContext.forcePrimary();
            List<RuntimeFeatureToggle> toggles = toggleMapper.findAll();
            runtimeToggleManager.apply(toggles);
            return new RuntimeToggleSnapshot(
                    runtimeToggleManager.currentVersion(),
                    runtimeToggleManager.currentSnapshot(),
                    runtimeToggleManager.refreshedAt()
            );
        } finally {
            DbRouteContext.clear();
        }
    }

    @Transactional(readOnly = true)
    public RuntimeToggleSnapshot getAuthoritativeSnapshot() {
        return refreshSnapshot();
    }

    @Transactional(readOnly = true)
    public List<RuntimeToggleAuditBatchView> recentHistory(int limit) {
        try {
            DbRouteContext.forcePrimary();
            return auditMapper.findRecentBatchViews(limit);
        } finally {
            DbRouteContext.clear();
        }
    }

    @Transactional
    public RuntimeToggleSnapshot updateFeature(RuntimeFeatureKey key, boolean enabled, String reason, RuntimeOperator operator) {
        String trimmedReason = normalizeReason(reason);
        String batchId = UUID.randomUUID().toString();
        try {
            DbRouteContext.forcePrimary();
            RuntimeFeatureToggle current = toggleMapper.findByFeatureKey(key.name());
            boolean oldValue = current == null ? key.defaultEnabled() : Boolean.TRUE.equals(current.getEnabled());
            long nextVersion = (current == null || current.getVersion() == null ? 0L : current.getVersion()) + 1L;
            upsertFeature(key, enabled, nextVersion, operator, trimmedReason, null);
            insertAudit(batchId, key.name(), oldValue, enabled, operator, trimmedReason, null);
        } finally {
            DbRouteContext.clear();
        }
        runtimeControlMetrics.recordApply("single");
        return refreshSnapshot();
    }

    @Transactional
    public RuntimeToggleSnapshot applyPreset(RuntimeFeaturePreset preset, String reason, RuntimeOperator operator) {
        String trimmedReason = normalizeReason(reason);
        String batchId = UUID.randomUUID().toString();
        try {
            DbRouteContext.forcePrimary();
            Map<RuntimeFeatureKey, Boolean> target = preset.resolveState();
            for (Map.Entry<RuntimeFeatureKey, Boolean> entry : target.entrySet()) {
                RuntimeFeatureToggle current = toggleMapper.findByFeatureKey(entry.getKey().name());
                boolean oldValue = current == null ? entry.getKey().defaultEnabled() : Boolean.TRUE.equals(current.getEnabled());
                long nextVersion = (current == null || current.getVersion() == null ? 0L : current.getVersion()) + 1L;
                upsertFeature(entry.getKey(), entry.getValue(), nextVersion, operator, trimmedReason, preset.name());
                insertAudit(batchId, entry.getKey().name(), oldValue, entry.getValue(), operator, trimmedReason, preset.name());
            }
        } finally {
            DbRouteContext.clear();
        }
        runtimeControlMetrics.recordApply("preset");
        runtimeControlMetrics.recordPreset(preset.name());
        return refreshSnapshot();
    }

    @Transactional
    public RuntimeToggleSnapshot rollbackBatch(String batchId, String reason, RuntimeOperator operator) {
        String trimmedReason = normalizeReason(reason);
        List<RuntimeFeatureToggleAudit> audits;
        try {
            DbRouteContext.forcePrimary();
            audits = auditMapper.findByBatchId(batchId);
            if (audits.isEmpty()) {
                throw new ExamBusinessException(404, "未找到对应的运行时控制批次");
            }
            String rollbackBatchId = UUID.randomUUID().toString();
            for (RuntimeFeatureToggleAudit audit : audits) {
                RuntimeFeatureKey key = RuntimeFeatureKey.from(audit.getFeatureKey());
                RuntimeFeatureToggle current = toggleMapper.findByFeatureKey(key.name());
                long nextVersion = (current == null || current.getVersion() == null ? 0L : current.getVersion()) + 1L;
                upsertFeature(key, Boolean.TRUE.equals(audit.getOldEnabled()), nextVersion, operator, trimmedReason, "ROLLBACK");
                insertAudit(rollbackBatchId, key.name(), current != null && Boolean.TRUE.equals(current.getEnabled()), Boolean.TRUE.equals(audit.getOldEnabled()), operator, trimmedReason, "ROLLBACK");
            }
        } finally {
            DbRouteContext.clear();
        }
        runtimeControlMetrics.recordApply("rollback");
        return refreshSnapshot();
    }

    public RuntimeToggleSnapshot getLocalSnapshot() {
        return new RuntimeToggleSnapshot(
                runtimeToggleManager.currentVersion(),
                runtimeToggleManager.currentSnapshot(),
                runtimeToggleManager.refreshedAt()
        );
    }

    private void upsertFeature(RuntimeFeatureKey key,
                               boolean enabled,
                               long version,
                               RuntimeOperator operator,
                               String reason,
                               String presetName) {
        RuntimeFeatureToggle toggle = new RuntimeFeatureToggle();
        toggle.setFeatureKey(key.name());
        toggle.setEnabled(enabled);
        toggle.setVersion(version);
        toggle.setUpdatedById(operator.getUserId());
        toggle.setUpdatedByRole(operator.getRole());
        toggle.setUpdatedByName(operator.getDisplayName());
        toggle.setUpdatedReason(reason);
        toggle.setPresetName(presetName);
        toggle.setUpdatedAt(LocalDateTime.now());
        toggleMapper.upsert(toggle);
    }

    private void insertAudit(String batchId,
                             String featureKey,
                             boolean oldValue,
                             boolean newValue,
                             RuntimeOperator operator,
                             String reason,
                             String presetName) {
        RuntimeFeatureToggleAudit audit = new RuntimeFeatureToggleAudit();
        audit.setBatchId(batchId);
        audit.setFeatureKey(featureKey);
        audit.setOldEnabled(oldValue);
        audit.setNewEnabled(newValue);
        audit.setOperatorId(operator.getUserId());
        audit.setOperatorRole(operator.getRole());
        audit.setOperatorName(operator.getDisplayName());
        audit.setReason(reason);
        audit.setPresetName(presetName);
        audit.setCreatedAt(LocalDateTime.now());
        auditMapper.insert(audit);
    }

    private String normalizeReason(String reason) {
        String trimmed = reason == null ? "" : reason.trim();
        if (trimmed.isBlank()) {
            throw new ExamBusinessException(400, "请填写切换原因");
        }
        return trimmed.length() > 255 ? trimmed.substring(0, 255) : trimmed;
    }
}
