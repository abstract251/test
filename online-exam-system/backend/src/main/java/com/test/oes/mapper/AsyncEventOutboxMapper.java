package com.test.oes.mapper;

import com.test.oes.entity.AsyncEventOutbox;
import org.apache.ibatis.annotations.*;

import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface AsyncEventOutboxMapper {

    int insert(AsyncEventOutbox row);

    AsyncEventOutbox findByEventId(@Param("eventId") String eventId);

    List<AsyncEventOutbox> lockBatchForPublish(@Param("now") LocalDateTime now, @Param("batchSize") int batchSize);

    int updateStatusBatch(@Param("ids") List<Long> ids, @Param("status") String status);

    int markPublished(@Param("id") Long id, @Param("publishedAt") LocalDateTime publishedAt);

    int markRetryOrFailed(@Param("id") Long id,
                          @Param("status") String status,
                          @Param("retryCount") Integer retryCount,
                          @Param("nextRetryAt") LocalDateTime nextRetryAt,
                          @Param("lastError") String lastError);

    long countPending();
}
