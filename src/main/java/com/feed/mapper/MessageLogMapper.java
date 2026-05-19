package com.feed.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.feed.entity.MessageLog;
import lombok.Data;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

import java.util.Date;

@Mapper
public interface MessageLogMapper extends BaseMapper<MessageLog> {
    @Update("UPDATE message_log SET status=#{status} WHERE id=#{id}")
    void updateStatus(@Param("id") Long id,@Param("status") String status);

    @Update("UPDATE message_log SET retry_count = retry_count + 1, next_retry_time = #{nextRetryTime} WHERE id = #{id}")
    void updateRetry(@Param("id") Long id, @Param("nextRetryTime") Date nextRetryTime);

}
