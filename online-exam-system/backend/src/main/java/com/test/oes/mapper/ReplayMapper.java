package com.test.oes.mapper;

import com.test.oes.entity.Replay;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface ReplayMapper {

    @Select("select messageId,replayId,replay,replayTime from replay")
    List<Replay> findAll();

    @Select("select messageId,replayId,replay,replayTime from replay where messageId = #{messageId}")
    List<Replay> findAllById(Integer messageId);

    @Select({
            "<script>",
            "select messageId, replayId, replay, replayTime from replay",
            "where messageId in",
            "<foreach collection='messageIds' item='messageId' open='(' separator=',' close=')'>",
            "#{messageId}",
            "</foreach>",
            "order by messageId asc, replayId asc",
            "</script>"
    })
    List<Replay> findByMessageIds(@Param("messageIds") List<Integer> messageIds);

    @Select("select messageId,replayId,replay,replayTime from replay where messageId = #{messageId}")
    Replay findById(Integer messageId);

    @Delete("delete from replay where replayId = #{replayId}")
    int delete(Integer replayId);

    @Update("update replay set replay = #{replay}, replayTime = #{replayTime} where replayId = #{replayId}")
    int update(Replay replay);

    @Options(useGeneratedKeys = true,keyProperty = "replayId")
    @Insert("insert into replay(messageId,replay,replayTime) values(#{messageId}, #{replay},#{replayTime})")
    int add(Replay replay);
}
