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

    List<RuntimeFeatureToggle> findAll();

    Long findMaxVersion();

    RuntimeFeatureToggle findByFeatureKey(@Param("featureKey") String featureKey);

    int upsert(RuntimeFeatureToggle toggle);
}
