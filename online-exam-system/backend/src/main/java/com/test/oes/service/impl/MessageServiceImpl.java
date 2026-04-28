package com.test.oes.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.test.oes.async.AsyncEventEnvelope;
import com.test.oes.async.AsyncEventTypes;
import com.test.oes.async.AsyncRoutingKeys;
import com.test.oes.async.OutboxEventService;
import com.test.oes.async.payload.MessageCreatedPayload;
import com.test.oes.cache.MessageCacheFacade;
import com.test.oes.entity.Message;
import com.test.oes.entity.Replay;
import com.test.oes.mapper.MessageMapper;
import com.test.oes.mapper.ReplayMapper;
import com.test.oes.security.CurrentUserService;
import com.test.oes.service.MessageService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MessageServiceImpl implements MessageService {

    private static final ZoneId SHANGHAI = ZoneId.of("Asia/Shanghai");

    private final MessageMapper messageMapper;
    private final ReplayMapper replayMapper;
    private final CurrentUserService currentUserService;
    private final OutboxEventService outboxEventService;
    private final MessageCacheFacade messageCacheFacade;

    @Override
    @Transactional(readOnly = true)
    public IPage<Message> findAll(Page page) {
        return messageCacheFacade.getMessagePage(page.getCurrent() <= 0 ? 1 : (int) page.getCurrent(), page.getSize() <= 0 ? 10 : (int) page.getSize(), () -> {
            IPage<Message> result = messageMapper.findAll(page);
            attachReplays(result.getRecords());
            return (Page<Message>) result;
        });
    }

    @Override
    @Transactional(readOnly = true)
    public Message findById(Integer id) {
        return messageCacheFacade.getMessageDetail(id, () -> {
            Message message = messageMapper.findById(id);
            if (message == null) {
                return null;
            }
            message.setReplays(messageCacheFacade.getMessageReplies(id, () -> replayMapper.findAllById(id)));
            return message;
        });
    }

    @Override
    public int delete(Integer id) {
        int rows = messageMapper.delete(id);
        if (rows > 0) {
            messageCacheFacade.bumpMessageFeedVersion();
            messageCacheFacade.evictMessageDetail(id);
            messageCacheFacade.evictMessageReplies(id);
        }
        return rows;
    }

    @Override
    public int update(Message message) {
        message.setUpdatedAt(LocalDateTime.now(SHANGHAI));
        int rows = messageMapper.update(message);
        if (rows > 0 && message.getId() != null) {
            messageCacheFacade.bumpMessageFeedVersion();
            messageCacheFacade.evictMessageDetail(message.getId());
            messageCacheFacade.evictMessageReplies(message.getId());
        }
        return rows;
    }

    @Override
    @Transactional
    public int add(Message message) {
        var currentUser = currentUserService.requireCurrentUser();
        LocalDateTime now = LocalDateTime.now(SHANGHAI);
        message.setTime(java.sql.Date.valueOf(now.toLocalDate()));
        message.setCreatorId(currentUser.getUserId());
        message.setCreatorRole(currentUser.getAccountRole().name());
        message.setCreatorName(currentUser.getDisplayName());
        message.setCreatedAt(now);
        message.setUpdatedAt(now);
        int rows = messageMapper.add(message);
        if (rows > 0 && message.getId() != null) {
            messageCacheFacade.bumpMessageFeedVersion();
            MessageCreatedPayload payload = new MessageCreatedPayload();
            payload.setMessageId(message.getId());
            payload.setTitle(message.getTitle());
            payload.setCreatorId(message.getCreatorId());
            payload.setCreatorRole(message.getCreatorRole());
            payload.setCreatorName(message.getCreatorName());
            payload.setCreatedAt(message.getCreatedAt());
            AsyncEventEnvelope<MessageCreatedPayload> envelope = new AsyncEventEnvelope<>();
            envelope.setEventType(AsyncEventTypes.MESSAGE_CREATED);
            envelope.setAggregateType("message");
            envelope.setAggregateId(String.valueOf(message.getId()));
            envelope.setOccurredAt(now);
            envelope.setPayloadVersion(1);
            envelope.setPayload(payload);
            outboxEventService.append(
                    AsyncEventTypes.MESSAGE_CREATED,
                    "message",
                    String.valueOf(message.getId()),
                    AsyncRoutingKeys.MESSAGE_CREATED,
                    envelope
            );
        }
        return rows;
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
