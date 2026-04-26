package com.test.oes.mapper;

import com.test.oes.entity.Replay;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface ReplayMapper {

    String REPLAY_COLUMNS = "messageId, replayId, replay, replayTime, creator_id as creatorId, "
            + "creator_role as creatorRole, creator_name as creatorName, created_at as createdAt";

    List<Replay> findAll();

    List<Replay> findAllById(Integer messageId);

    List<Replay> findByMessageIds(@Param("messageIds") List<Integer> messageIds);

    Replay findById(Integer messageId);

    int delete(Integer replayId);

    int update(Replay replay);

    int add(Replay replay);
}
