package com.test.oes.mapper;

import com.test.oes.runtime.RuntimeFeatureToggle;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper
public interface RuntimeFeatureToggleMapper {

    @Select("SELECT feature_key, enabled, version, updated_by_id, updated_by_role, updated_by_name, updated_reason, preset_name, updated_at FROM runtime_feature_toggle ORDER BY feature_key")
    List<RuntimeFeatureToggle> findAll();

    @Select("SELECT COALESCE(MAX(version), 0) FROM runtime_feature_toggle")
    Long findMaxVersion();

    @Select("SELECT feature_key, enabled, version, updated_by_id, updated_by_role, updated_by_name, updated_reason, preset_name, updated_at FROM runtime_feature_toggle WHERE feature_key = #{featureKey}")
    RuntimeFeatureToggle findByFeatureKey(@Param("featureKey") String featureKey);

    @Insert("""
            INSERT INTO runtime_feature_toggle(feature_key, enabled, version, updated_by_id, updated_by_role, updated_by_name, updated_reason, preset_name, updated_at)
            VALUES(#{featureKey}, #{enabled}, #{version}, #{updatedById}, #{updatedByRole}, #{updatedByName}, #{updatedReason}, #{presetName}, #{updatedAt})
            ON DUPLICATE KEY UPDATE
                enabled = VALUES(enabled),
                version = VALUES(version),
                updated_by_id = VALUES(updated_by_id),
                updated_by_role = VALUES(updated_by_role),
                updated_by_name = VALUES(updated_by_name),
                updated_reason = VALUES(updated_reason),
                preset_name = VALUES(preset_name),
                updated_at = VALUES(updated_at)
            """)
    int upsert(RuntimeFeatureToggle toggle);
}
