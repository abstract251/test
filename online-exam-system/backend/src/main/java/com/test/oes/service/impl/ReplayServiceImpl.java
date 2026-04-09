package com.test.oes.service.impl;

import com.test.oes.entity.Replay;
import com.test.oes.mapper.ReplayMapper;
import com.test.oes.service.ReplayService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ReplayServiceImpl implements ReplayService {

    private final ReplayMapper replayMapper;

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
        return replayMapper.add(replay);
    }
}