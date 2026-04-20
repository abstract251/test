package com.test.oes.mapper;

import com.test.oes.runtime.RuntimeFeatureToggleAudit;
import com.test.oes.runtime.RuntimeToggleAuditBatchView;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface RuntimeFeatureToggleAuditMapper {

    @Insert("""
            INSERT INTO runtime_feature_toggle_audit(batch_id, feature_key, old_enabled, new_enabled, operator_id, operator_role, operator_name, reason, preset_name, created_at)
            VALUES(#{batchId}, #{featureKey}, #{oldEnabled}, #{newEnabled}, #{operatorId}, #{operatorRole}, #{operatorName}, #{reason}, #{presetName}, #{createdAt})
            """)
    int insert(RuntimeFeatureToggleAudit audit);

    @Select("""
            SELECT id, batch_id, feature_key, old_enabled, new_enabled, operator_id, operator_role, operator_name, reason, preset_name, created_at
            FROM runtime_feature_toggle_audit
            WHERE batch_id = #{batchId}
            ORDER BY id
            """)
    List<RuntimeFeatureToggleAudit> findByBatchId(@Param("batchId") String batchId);

    @Select("""
            SELECT batch_id, MAX(preset_name) AS preset_name, MAX(reason) AS reason, MAX(operator_id) AS operator_id,
                   MAX(operator_role) AS operator_role, MAX(operator_name) AS operator_name,
                   COUNT(*) AS change_count, MAX(created_at) AS created_at
            FROM runtime_feature_toggle_audit
            GROUP BY batch_id
            ORDER BY MAX(created_at) DESC
            LIMIT #{limit}
            """)
    List<RuntimeToggleAuditBatchView> findRecentBatchViews(@Param("limit") int limit);
}
