package com.feed.mq;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.feed.entity.Article;
import com.feed.entity.Follow;
import com.feed.entity.User;
import com.feed.mapper.ArticleMapper;
import com.feed.mapper.FollowMapper;
import com.feed.mapper.UserMapper;
import com.feed.service.ArticleService;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import com.feed.config.RabbitMQConfig;

import java.util.List;

/**
 * 推模式，给小V和大V的活跃粉丝使用推模式
 */
@Component
public class FeedConsumer {

    @Autowired
    private ArticleService articleService;
    @Autowired
    private ArticleMapper articleMapper;
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private FollowMapper followMapper;
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @RabbitListener(queues = RabbitMQConfig.ARTICLE_QUEUE)
    public void handleArticleCreate(Long articleId) {

        // 1. 查询文章
        Article article = articleMapper.selectById(articleId);
        if (article == null) {
            return;
        }

        //2.判断是大V还是小V
        User author = userMapper.selectById(article.getAuthorId());
        boolean isBigV = author.getFanCount() >= 10000;
        //3. 获取作者的粉丝列表
        List<Follow> fans = followMapper.selectList(new LambdaQueryWrapper<Follow>()
                .eq(Follow::getStatus,1)
                .eq(Follow::getFollowId,article.getAuthorId())
        );

        //4.判断是否为活跃用户
        long activeThreshold = System.currentTimeMillis() - 7 * 24 * 3600 * 1000;

        // 5. 推送文章到粉丝的收件箱（Redis ZSet）
        int skippedCount=0;
        int pushedCount=0;

        for (Follow fan : fans) {
            // 小V（粉丝数 < 10000）：全推，不区分活跃度
            if (!isBigV) {
                String key = "inbox:" + fan.getUserId();
                redisTemplate.opsForZSet().add(key, articleId, article.getCreateTime().getTime());
                pushedCount++;
                continue;
            }

            // 大V：只推活跃粉丝
            User fanUser = userMapper.selectById(fan.getUserId());
            if (fanUser == null) continue;

            boolean isActive = fanUser.getLastLoginTime() != null &&
                    fanUser.getLastLoginTime().getTime() > activeThreshold;

            if (isActive) {
                String key = "inbox:" + fan.getUserId();
                redisTemplate.opsForZSet().add(key, articleId, article.getCreateTime().getTime());
                pushedCount++;
                System.out.println("已推送第"+pushedCount+"个粉丝");
            } else {
                skippedCount++;
                System.out.println("粉丝 " + fan.getUserId() + " 不活跃，跳过推送"+skippedCount+"个");
            }
        }
    }
}