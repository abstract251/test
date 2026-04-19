package com.test.oes.async;

import com.test.oes.entity.AsyncEventConsumeRecord;
import com.test.oes.mapper.AsyncEventConsumeRecordMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class AsyncConsumeSupport {

    private final AsyncEventConsumeRecordMapper consumeRecordMapper;

    public boolean tryStart(String consumerName, String eventId, String eventType) {
        try {
            AsyncEventConsumeRecord row = new AsyncEventConsumeRecord();
            row.setConsumerName(consumerName);
            row.setEventId(eventId);
            row.setEventType(eventType);
            row.setProcessedAt(LocalDateTime.now());
            row.setStatus("PROCESSING");
            consumeRecordMapper.insert(row);
            return true;
        } catch (DuplicateKeyException duplicateKeyException) {
            AsyncEventConsumeRecord existing = consumeRecordMapper.findByConsumerAndEvent(consumerName, eventId);
            if (existing != null && "SUCCESS".equals(existing.getStatus())) {
                return false;
            }
            consumeRecordMapper.updateStatus(consumerName, eventId, LocalDateTime.now(), "PROCESSING", null);
            return true;
        }
    }

    public void markSuccess(String consumerName, String eventId) {
        consumeRecordMapper.updateStatus(consumerName, eventId, LocalDateTime.now(), "SUCCESS", null);
    }

    public void markFailed(String consumerName, String eventId, String error) {
        String message = error == null ? null : error.substring(0, Math.min(900, error.length()));
        consumeRecordMapper.updateStatus(consumerName, eventId, LocalDateTime.now(), "FAILED", message);
    }
}
