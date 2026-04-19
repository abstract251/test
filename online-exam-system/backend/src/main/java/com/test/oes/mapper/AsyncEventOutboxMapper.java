package com.test.oes.mapper;

import com.test.oes.entity.AsyncEventOutbox;
import org.apache.ibatis.annotations.*;

import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface AsyncEventOutboxMapper {

    @Options(useGeneratedKeys = true, keyProperty = "id")
    @Insert("""
            INSERT INTO async_event_outbox(
                event_id, event_type, aggregate_type, aggregate_id, routing_key, payload_json,
                status, retry_count, next_retry_at, published_at, last_error, created_at
            ) VALUES (
                #{eventId}, #{eventType}, #{aggregateType}, #{aggregateId}, #{routingKey}, #{payloadJson},
                #{status}, #{retryCount}, #{nextRetryAt}, #{publishedAt}, #{lastError}, #{createdAt}
            )
            """)
    int insert(AsyncEventOutbox row);

    @Select("""
            SELECT id, event_id, event_type, aggregate_type, aggregate_id, routing_key, payload_json,
                   status, retry_count, next_retry_at, published_at, last_error, created_at
            FROM async_event_outbox
            WHERE event_id = #{eventId}
            LIMIT 1
            """)
    AsyncEventOutbox findByEventId(@Param("eventId") String eventId);

    @Select("""
            <script>
            SELECT id, event_id, event_type, aggregate_type, aggregate_id, routing_key, payload_json,
                   status, retry_count, next_retry_at, published_at, last_error, created_at
            FROM async_event_outbox
            WHERE status IN ('NEW', 'RETRY')
              AND (next_retry_at IS NULL OR next_retry_at &lt;= #{now})
            ORDER BY id
            LIMIT #{batchSize}
            FOR UPDATE SKIP LOCKED
            </script>
            """)
    List<AsyncEventOutbox> lockBatchForPublish(@Param("now") LocalDateTime now, @Param("batchSize") int batchSize);

    @Update("""
            <script>
            UPDATE async_event_outbox
            SET status = #{status}
            WHERE id IN
            <foreach collection='ids' item='id' open='(' separator=',' close=')'>
                #{id}
            </foreach>
            </script>
            """)
    int updateStatusBatch(@Param("ids") List<Long> ids, @Param("status") String status);

    @Update("""
            UPDATE async_event_outbox
            SET status = 'PUBLISHED',
                published_at = #{publishedAt},
                last_error = NULL
            WHERE id = #{id}
            """)
    int markPublished(@Param("id") Long id, @Param("publishedAt") LocalDateTime publishedAt);

    @Update("""
            UPDATE async_event_outbox
            SET status = #{status},
                retry_count = #{retryCount},
                next_retry_at = #{nextRetryAt},
                last_error = #{lastError}
            WHERE id = #{id}
            """)
    int markRetryOrFailed(@Param("id") Long id,
                          @Param("status") String status,
                          @Param("retryCount") Integer retryCount,
                          @Param("nextRetryAt") LocalDateTime nextRetryAt,
                          @Param("lastError") String lastError);

    @Select("""
            SELECT COUNT(*)
            FROM async_event_outbox
            WHERE status IN ('NEW', 'RETRY', 'PROCESSING')
            """)
    long countPending();
}
