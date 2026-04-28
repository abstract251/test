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

    int insert(RuntimeFeatureToggleAudit audit);

    List<RuntimeFeatureToggleAudit> findByBatchId(@Param("batchId") String batchId);

    List<RuntimeToggleAuditBatchView> findRecentBatchViews(@Param("limit") int limit);
}
