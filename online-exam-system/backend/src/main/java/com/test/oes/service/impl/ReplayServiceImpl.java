package com.test.oes.service.impl;

import com.test.oes.cache.MessageCacheFacade;
import com.test.oes.entity.Replay;
import com.test.oes.mapper.ReplayMapper;
import com.test.oes.security.CurrentUserService;
import com.test.oes.service.ReplayService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ReplayServiceImpl implements ReplayService {

    private static final ZoneId SHANGHAI = ZoneId.of("Asia/Shanghai");

    private final ReplayMapper replayMapper;
    private final CurrentUserService currentUserService;
    private final MessageCacheFacade messageCacheFacade;

    @Override
    public List<Replay> findAll() {
        return replayMapper.findAll();
    }

    @Override
    public List<Replay> findAllById(Integer messageId) {
        return replayMapper.findAllById(messageId);
    }

    @Override
    public Replay findById(Integer replayId) {
        return replayMapper.findById(replayId);
    }

    @Override
    public int delete(Integer replayId) {
        return replayMapper.delete(replayId);
    }

    @Override
    public int update(Replay replay) {
        return replayMapper.update(replay);
    }

    @Override
    public int add(Replay replay) {
        var currentUser = currentUserService.requireCurrentUser();
        LocalDateTime now = LocalDateTime.now(SHANGHAI);
        replay.setReplayTime(java.sql.Date.valueOf(now.toLocalDate()));
        replay.setCreatorId(currentUser.getUserId());
        replay.setCreatorRole(currentUser.getAccountRole().name());
        replay.setCreatorName(currentUser.getDisplayName());
        replay.setCreatedAt(now);
        int rows = replayMapper.add(replay);
        if (rows > 0 && replay.getMessageId() != null) {
            messageCacheFacade.bumpMessageFeedVersion();
            messageCacheFacade.evictMessageDetail(replay.getMessageId());
            messageCacheFacade.evictMessageReplies(replay.getMessageId());
        }
        return rows;
    }
}
