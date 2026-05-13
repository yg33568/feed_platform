package com.feed.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.feed.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface UserMapper extends BaseMapper<User> {
    @Update("UPDATE user SET fan_count = fan_count + #{delta} WHERE id = #{id}")
    void incrFanCount(@Param("id") Long id, @Param("delta") int delta);
}