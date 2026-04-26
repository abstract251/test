package com.test.oes.mapper;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.test.oes.entity.Message;
import org.apache.ibatis.annotations.*;

@Mapper
public interface MessageMapper {
    String MESSAGE_COLUMNS = "id, id as temp_id, title, content, time, "
            + "creator_id as creatorId, creator_role as creatorRole, creator_name as creatorName, "
            + "created_at as createdAt, updated_at as updatedAt";

    IPage<Message> findAll(Page page);

    Message findById(Integer id);

    int delete(Integer id);

    int update(Message message);

    int add(Message message);
}
