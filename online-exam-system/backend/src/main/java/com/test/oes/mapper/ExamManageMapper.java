package com.test.oes.mapper;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.test.oes.entity.ExamManage;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface ExamManageMapper {

    @Select("select * from exam_manage")
    IPage<ExamManage> findAll(Page<ExamManage> page);

    @Select("select * from exam_manage where examCode = #{examCode}")
    ExamManage findById(Integer examCode);

    @Select("select * from exam_manage where paperId = #{paperId}")
    List<ExamManage> findByPaperId(@Param("paperId") Integer paperId);

    @Delete("delete from exam_manage where examCode = #{examCode}")
    int delete(Integer examCode);

    @Update("update exam_manage set description=#{description}, source=#{source}, paperId=#{paperId}, "
            + "examDate=#{examDate}, exam_start_at=#{examStartAt}, totalTime=#{totalTime}, grade=#{grade}, term=#{term}, "
            + "major=#{major}, institute=#{institute}, totalScore=#{totalScore}, type=#{type}, tips=#{tips}, "
            + "paper_frozen_at=#{paperFrozenAt}, revoked_at=#{revokedAt}, revoke_reason=#{revokeReason} "
            + "where examCode=#{examCode}")
    int update(ExamManage exammanage);

    @Update("update exam_manage set description=#{description}, tips=#{tips}, totalTime=#{totalTime} where examCode=#{examCode}")
    int updateWhitelist(@Param("examCode") Integer examCode,
                        @Param("description") String description,
                        @Param("tips") String tips,
                        @Param("totalTime") Integer totalTime);

    @Update("update exam_manage set paper_frozen_at=#{paperFrozenAt} where examCode=#{examCode}")
    int updatePaperFrozenAt(@Param("examCode") Integer examCode, @Param("paperFrozenAt") java.time.LocalDateTime paperFrozenAt);

    @Update("update exam_manage set revoked_at=#{revokedAt}, revoke_reason=#{reason} where examCode=#{examCode}")
    int markRevoked(@Param("examCode") Integer examCode,
                    @Param("revokedAt") java.time.LocalDateTime revokedAt,
                    @Param("reason") String reason);

    @Options(useGeneratedKeys = true, keyProperty = "examCode")
    @Insert("insert into exam_manage(description,source,paperId,examDate,exam_start_at,totalTime,grade,term,major,institute,"
            + "totalScore,type,tips,paper_frozen_at,revoked_at,revoke_reason) "
            + "values(#{description},#{source},#{paperId},#{examDate},#{examStartAt},#{totalTime},#{grade},#{term},#{major},"
            + "#{institute},#{totalScore},#{type},#{tips},#{paperFrozenAt},#{revokedAt},#{revokeReason})")
    int add(ExamManage exammanage);

    /**
     * 查询最后一条记录的paperId,返回给前端达到自增效果
     * @return paperId
     */
    @Select("select paperId from exam_manage order by paperId desc limit 1")
    ExamManage findOnlyPaperId();
}
