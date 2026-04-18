package com.test.oes.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.test.oes.entity.Message;
import com.test.oes.entity.Replay;
import com.test.oes.mapper.MessageMapper;
import com.test.oes.mapper.ReplayMapper;
import com.test.oes.service.MessageService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MessageServiceImpl implements MessageService {

    private final MessageMapper messageMapper;
    private final ReplayMapper replayMapper;

    @Override
    @Transactional(readOnly = true)
    public IPage<Message> findAll(Page page) {
        IPage<Message> result = messageMapper.findAll(page);
        attachReplays(result.getRecords());
        return result;
    }

    @Override
    @Transactional(readOnly = true)
    public Message findById(Integer id) {
        Message message = messageMapper.findById(id);
        if (message == null) {
            return null;
        }
        message.setReplays(replayMapper.findAllById(id));
        return message;
    }

    @Override
    public int delete(Integer id) {
        return messageMapper.delete(id);
    }

    @Override
    public int update(Message message) {
        return messageMapper.update(message);
    }

    @Override
    public int add(Message message) {
        return messageMapper.add(message);
    }

    private void attachReplays(List<Message> messages) {
        if (messages == null || messages.isEmpty()) {
            return;
        }
        List<Integer> messageIds = messages.stream()
                .map(Message::getId)
                .filter(id -> id != null)
                .toList();
        if (messageIds.isEmpty()) {
            return;
        }
        Map<Integer, List<Replay>> replayMap = replayMapper.findByMessageIds(messageIds).stream()
                .collect(Collectors.groupingBy(Replay::getMessageId));
        for (Message message : messages) {
            message.setReplays(replayMap.getOrDefault(message.getId(), List.of()));
        }
    }
}
