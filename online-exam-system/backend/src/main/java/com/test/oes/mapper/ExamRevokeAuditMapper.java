package com.test.oes.mapper;

import com.test.oes.entity.ExamRevokeAudit;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;

@Mapper
public interface ExamRevokeAuditMapper {

    @Options(useGeneratedKeys = true, keyProperty = "id")
    @Insert("INSERT INTO exam_revoke_audit(exam_code, admin_id, admin_name, reason, detail, created_at) "
            + "VALUES (#{examCode}, #{adminId}, #{adminName}, #{reason}, #{detail}, #{createdAt})")
    int insert(ExamRevokeAudit row);
}
