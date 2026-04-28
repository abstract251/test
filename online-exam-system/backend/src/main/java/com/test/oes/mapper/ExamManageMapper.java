package com.test.oes.mapper;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.test.oes.entity.ExamManage;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface ExamManageMapper {

    String EXAM_COLUMNS = "examCode, description, source, paperId, examDate, exam_start_at as examStartAt, "
            + "totalTime, grade, term, major, institute, totalScore, type, tips, "
            + "paper_frozen_at as paperFrozenAt, revoked_at as revokedAt, revoke_reason as revokeReason";

    IPage<ExamManage> findAll(Page<ExamManage> page);

    ExamManage findById(Integer examCode);

    List<ExamManage> findVisibleForStudent(@Param("grade") String grade,
                                           @Param("major") String major,
                                           @Param("institute") String institute);

    ExamManage findVisibleByExamCodeForStudent(@Param("examCode") Integer examCode,
                                               @Param("grade") String grade,
                                               @Param("major") String major,
                                               @Param("institute") String institute);

    List<ExamManage> findByPaperId(@Param("paperId") Integer paperId);

    int delete(Integer examCode);

    int update(ExamManage exammanage);

    int updateWhitelist(@Param("examCode") Integer examCode,
                        @Param("description") String description,
                        @Param("tips") String tips,
                        @Param("totalTime") Integer totalTime);

    int updatePaperFrozenAt(@Param("examCode") Integer examCode, @Param("paperFrozenAt") java.time.LocalDateTime paperFrozenAt);

    int markRevoked(@Param("examCode") Integer examCode,
                    @Param("revokedAt") java.time.LocalDateTime revokedAt,
                    @Param("reason") String reason);

    int add(ExamManage exammanage);

    /**
     * 查询最后一条记录的paperId,返回给前端达到自增效果
     * @return paperId
     */
    ExamManage findOnlyPaperId();
}
