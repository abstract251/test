package com.test.oes.mapper;

import com.test.oes.entity.ExamRevokeAudit;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;

@Mapper
public interface ExamRevokeAuditMapper {

    int insert(ExamRevokeAudit row);
}
