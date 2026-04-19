package com.test.oes.mapper;

import com.test.oes.entity.AsyncEventConsumeRecord;
import org.apache.ibatis.annotations.*;

import java.time.LocalDateTime;

@Mapper
public interface AsyncEventConsumeRecordMapper {

    @Options(useGeneratedKeys = true, keyProperty = "id")
    @Insert("""
            INSERT INTO async_event_consume_record(
                consumer_name, event_id, event_type, processed_at, status, error_message
            ) VALUES (
                #{consumerName}, #{eventId}, #{eventType}, #{processedAt}, #{status}, #{errorMessage}
            )
            """)
    int insert(AsyncEventConsumeRecord row);

    @Select("""
            SELECT id, consumer_name, event_id, event_type, processed_at, status, error_message
            FROM async_event_consume_record
            WHERE consumer_name = #{consumerName}
              AND event_id = #{eventId}
            LIMIT 1
            """)
    AsyncEventConsumeRecord findByConsumerAndEvent(@Param("consumerName") String consumerName,
                                                   @Param("eventId") String eventId);

    @Update("""
            UPDATE async_event_consume_record
            SET processed_at = #{processedAt},
                status = #{status},
                error_message = #{errorMessage}
            WHERE consumer_name = #{consumerName}
              AND event_id = #{eventId}
            """)
    int updateStatus(@Param("consumerName") String consumerName,
                     @Param("eventId") String eventId,
                     @Param("processedAt") LocalDateTime processedAt,
                     @Param("status") String status,
                     @Param("errorMessage") String errorMessage);
}
