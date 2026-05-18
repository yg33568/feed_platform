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

    @Update("UPDATE user SET last_login_time = NOW() WHERE id = #{userId}")
    void updateLastLoginTime(@Param("userId") Long userId);
}