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

    @Select("select " + MESSAGE_COLUMNS + " from message order by id desc")
    IPage<Message> findAll(Page page);

    @Select("select " + MESSAGE_COLUMNS + " from message where id = #{id}")
    Message findById(Integer id);

    @Delete("delete from message where id = #{id}")
    int delete(Integer id);

    @Update("update message set title = #{title}, content = #{content}, time = #{time}, updated_at = #{updatedAt} where id = #{id}")
    int update(Message message);

    @Options(useGeneratedKeys = true,keyProperty = "id")
    @Insert("""
            insert into message(title, content, time, creator_id, creator_role, creator_name, created_at, updated_at)
            values(#{title}, #{content}, #{time}, #{creatorId}, #{creatorRole}, #{creatorName}, #{createdAt}, #{updatedAt})
            """)
    int add(Message message);
}
