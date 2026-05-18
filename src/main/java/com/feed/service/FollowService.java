package com.feed.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.feed.entity.Follow;
import com.feed.mapper.FollowMapper;
import com.feed.mapper.UserMapper;
import com.feed.util.BloomFilterUtil;
import com.feed.util.SnowflakeIdUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;

@Service
//实现关注和取关的核心逻辑
public class FollowService {
    @Autowired
    private FollowMapper followMapper;
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private SnowflakeIdUtil snowflakeIdUtil;
    @Autowired
    private BloomFilterUtil bloomFilterUtil;

    /**
     * 关注作者
     */
    public void follow(Long userId,Long followId){
        // 布隆过滤器检查
        if (!bloomFilterUtil.mightContainUser(userId)) {
            throw new RuntimeException("当前用户不存在");
        }
        if (!bloomFilterUtil.mightContainUser(followId)) {
            throw new RuntimeException("被关注用户不存在");
        }
        //1.查询是否关注
        LambdaQueryWrapper<Follow> wrapper=new LambdaQueryWrapper<>();
        wrapper.eq(Follow::getFollowId ,followId)
                .eq(Follow::getUserId ,userId)
                .eq(Follow::getStatus,1);
        Follow exist = followMapper.selectOne(wrapper);
        //如果已经关注了，直接返回
        if(exist!=null){
            return;
        }
        //2.加入关注记录
        Follow follow=new Follow();
        follow.setFollowId(followId);
        follow.setUserId(userId);
        follow.setStatus(1);
        follow.setId(snowflakeIdUtil.nextId());
        follow.setCreateTime(new Date());
        followMapper.insert(follow);

        // 3. 更新作者的粉丝数 +1
        userMapper.incrFanCount(followId, 1);
    }

    /**
     * 取关作者
     */
    @Transactional
    public void unfollow(Long userId, Long followId) {
        // 布隆过滤器检查
        if (!bloomFilterUtil.mightContainUser(userId)) {
            throw new RuntimeException("当前用户不存在");
        }
        if (!bloomFilterUtil.mightContainUser(followId)) {
            throw new RuntimeException("被关注用户不存在");
        }
        // 1. 更新关注记录状态为 0（取关）
        LambdaQueryWrapper<Follow> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Follow::getUserId, userId)
                .eq(Follow::getFollowId, followId);
        Follow follow = followMapper.selectOne(wrapper);
        if (follow != null && follow.getStatus() == 1) {
            follow.setStatus(0);
            followMapper.updateById(follow);
            // 2. 更新作者的粉丝数 -1
            userMapper.incrFanCount(followId, -1);
        }
    }
    /**
     * 判断当前用户关注的作者是否是大V
     */
    public boolean hasBigV(Long userId){
        return followMapper.exists(
                new LambdaQueryWrapper<Follow>()
                        .eq(Follow::getUserId,userId)
                        .eq(Follow::getStatus,1)
                        .inSql(Follow::getFollowId,
                                "SELECT id FROM user WHERE is_big_v = 1 ")
                );
    }
}
