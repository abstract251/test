package com.test.oes.mapper;

import com.test.oes.entity.Replay;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface ReplayMapper {

    String REPLAY_COLUMNS = "messageId, replayId, replay, replayTime, creator_id as creatorId, "
            + "creator_role as creatorRole, creator_name as creatorName, created_at as createdAt";

    @Select("select " + REPLAY_COLUMNS + " from replay")
    List<Replay> findAll();

    @Select("select " + REPLAY_COLUMNS + " from replay where messageId = #{messageId}")
    List<Replay> findAllById(Integer messageId);

    @Select({
            "<script>",
            "select " + REPLAY_COLUMNS + " from replay",
            "where messageId in",
            "<foreach collection='messageIds' item='messageId' open='(' separator=',' close=')'>",
            "#{messageId}",
            "</foreach>",
            "order by messageId asc, replayId asc",
            "</script>"
    })
    List<Replay> findByMessageIds(@Param("messageIds") List<Integer> messageIds);

    @Select("select " + REPLAY_COLUMNS + " from replay where messageId = #{messageId}")
    Replay findById(Integer messageId);

    @Delete("delete from replay where replayId = #{replayId}")
    int delete(Integer replayId);

    @Update("update replay set replay = #{replay}, replayTime = #{replayTime}, created_at = #{createdAt} where replayId = #{replayId}")
    int update(Replay replay);

    @Options(useGeneratedKeys = true,keyProperty = "replayId")
    @Insert("""
            insert into replay(messageId, replay, replayTime, creator_id, creator_role, creator_name, created_at)
            values(#{messageId}, #{replay}, #{replayTime}, #{creatorId}, #{creatorRole}, #{creatorName}, #{createdAt})
            """)
    int add(Replay replay);
}
