package com.feed.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.feed.entity.Article;
import com.feed.entity.User;
import com.feed.mapper.ArticleMapper;
import com.feed.mapper.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CacheWarmUpService {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private ArticleMapper articleMapper;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    /**
     * 预热大V的收件箱
     * @param fanThreshold 粉丝数阈值（超过此值视为大V）
     */
    public void warmUpBigVInbox(int fanThreshold) {
        // 1. 查出所有大V
        List<User> bigVList = userMapper.selectList(
                new LambdaQueryWrapper<User>()
                        .ge(User::getFanCount, fanThreshold)
        );

        for (User bigV : bigVList) {
            // 2. 查出大V最近的文章（比如最近100篇）
            List<Article> articles = articleMapper.selectList(
                    new LambdaQueryWrapper<Article>()
                            .eq(Article::getAuthorId, bigV.getId())
                            .eq(Article::getStatus, 1)
                            .orderByDesc(Article::getCreateTime)
                            .last("LIMIT 100")
            );

            // 3. 写入 Redis ZSet 收件箱（key 是大V自己的收件箱，不是粉丝的）
            // 注意：这里预热的是「大V自己的收件箱」，方便大V自己刷首页
            String key = "inbox:" + bigV.getId();
            for (Article article : articles) {
                redisTemplate.opsForZSet().add(key, article.getId(), article.getCreateTime().getTime());
            }
            System.out.println("预热大V收件箱：" + bigV.getName() + "，文章数：" + articles.size());
        }
    }
}
