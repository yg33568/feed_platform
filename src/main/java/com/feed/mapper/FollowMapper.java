package com.feed.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.feed.entity.Follow;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface  FollowMapper extends BaseMapper <Follow> {
    }
