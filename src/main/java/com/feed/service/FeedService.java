package com.feed.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.feed.entity.Article;
import com.feed.entity.Follow;
import com.feed.entity.User;
import com.feed.mapper.ArticleMapper;
import com.feed.mapper.FollowMapper;
import com.feed.mapper.UserMapper;
import com.feed.util.BloomFilterUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class FeedService {

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;
    @Autowired
    private ArticleMapper articleMapper;
    @Autowired
    private BloomFilterUtil bloomFilterUtil;
    @Autowired
    private FollowMapper followMapper;
    @Autowired
    private UserMapper userMapper;

    //通过用户活跃度判断使用推或者拉
    public List<Article> selectMode(Long userId, int page, int size) {
        // 1. 布隆过滤器检查
        if (!bloomFilterUtil.mightContainUser(userId)) {
            return new ArrayList<>();
        }

        // 2. 查询当前用户是否活跃
        User user = userMapper.selectById(userId);
        boolean isActive = isUserActive(user);

        // 3. 根据活跃度选择模式
        if (isActive) {
            System.out.println("用户 " + userId + " 活跃，走推模式");
            return getFeed(userId, page, size);
        } else {
            System.out.println("用户 " + userId + " 不活跃，走拉模式");
            return pullFeed(userId, page, size);
        }
    }

    private boolean isUserActive(User user) {
        if (user == null || user.getLastLoginTime() == null) {
            return false;
        }
        long sevenDaysAgo = System.currentTimeMillis() - 7 * 24 * 3600 * 1000;
        return user.getLastLoginTime().getTime() > sevenDaysAgo;
    }

    /**
     *推模式：从Redis ZSet收件箱读取
     * @param userId
     * @param page
     * @param size
     * @return
     */
    public List<Article> getFeed(Long userId, int page, int size) {
        // 布隆过滤器检查
        if (!bloomFilterUtil.mightContainUser(userId)) {
            return new ArrayList<>();
        }

        //分页计算
        int start = (page - 1) * size;
        int end = start + size - 1;
        //返回：文章ID的集合
        Set<Object> articleIds = redisTemplate.opsForZSet()
                .reverseRange("inbox:" + userId, start, end);


        if (articleIds == null || articleIds.isEmpty()) {
            return new ArrayList<>();
        }

        List<Long> ids = new ArrayList<>();
        for (Object id : articleIds) {
            ids.add(Long.valueOf(id.toString()));
        }

        return articleMapper.selectBatchIds(ids);
    }

    /**
     * 拉模式：实时查询关注作者的文章（用于大V场景）
     */
    public List<Article> pullFeed(Long userId,int page,int size){
        List<Follow> followList= followMapper.selectList(
                new LambdaQueryWrapper<Follow>()
                        .eq(Follow::getUserId,userId)
                        .eq(Follow::getStatus,1)
        );
        if(followList.isEmpty()){
            return new ArrayList<>();
        }
        List<Long> authorIds = followList.stream()
                .map(Follow::getFollowId)
                .collect(Collectors.toList());
        // 2. 分页查询文章（按时间倒序）
        Page<Article> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<Article> wrapper = new LambdaQueryWrapper<>();
        wrapper.in(Article::getAuthorId, authorIds)
                .orderByDesc(Article::getCreateTime);

        return articleMapper.selectPage(pageParam, wrapper).getRecords();
    }

}
