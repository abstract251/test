package com.test.oes.mapper;

import com.test.oes.entity.AsyncEventConsumeRecord;
import org.apache.ibatis.annotations.*;

import java.time.LocalDateTime;

@Mapper
public interface AsyncEventConsumeRecordMapper {

    int insert(AsyncEventConsumeRecord row);

    AsyncEventConsumeRecord findByConsumerAndEvent(@Param("consumerName") String consumerName,
                                                   @Param("eventId") String eventId);

    int updateStatus(@Param("consumerName") String consumerName,
                     @Param("eventId") String eventId,
                     @Param("processedAt") LocalDateTime processedAt,
                     @Param("status") String status,
                     @Param("errorMessage") String errorMessage);
}
