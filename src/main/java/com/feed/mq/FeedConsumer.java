package com.feed.mq;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.feed.entity.Article;
import com.feed.entity.Follow;
import com.feed.entity.User;
import com.feed.mapper.ArticleMapper;
import com.feed.mapper.FollowMapper;
import com.feed.mapper.UserMapper;
import com.feed.config.RabbitMQConfig;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class FeedConsumer {

    @Autowired
    private ArticleMapper articleMapper;
    @Autowired
    private FollowMapper followMapper;
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @RabbitListener(queues = RabbitMQConfig.ARTICLE_QUEUE)
    public void handleArticleCreate(Long articleId) {  // 去掉 Channel 和 deliveryTag
        try {
            // 1. 查询文章
            Article article = articleMapper.selectById(articleId);
            if (article == null) {
                return;
            }

            // 2. 获取作者
            User author = userMapper.selectById(article.getAuthorId());
            if (author == null) {
                return;
            }

            // 3. 判断是否大V（粉丝数 >= 10000）
            boolean isBigV = author.getFanCount() != null && author.getFanCount() >= 10000;

            // 4. 获取粉丝列表
            List<Follow> fans = followMapper.selectList(
                    new LambdaQueryWrapper<Follow>()
                            .eq(Follow::getFollowId, article.getAuthorId())
                            .eq(Follow::getStatus, 1)
            );

            // 5. 活跃阈值（7天内登录）
            long activeThreshold = System.currentTimeMillis() - 7 * 24 * 3600 * 1000;
            int pushedCount = 0;
            int skippedCount = 0;

            for (Follow fan : fans) {
                // 小V：全推
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
                } else {
                    skippedCount++;
                }
            }

            System.out.println("推送完成：作者ID=" + article.getAuthorId() +
                    "，大V=" + isBigV +
                    "，推送=" + pushedCount +
                    "，跳过=" + skippedCount);

        } catch (Exception e) {
            e.printStackTrace();
            // 自动 ACK 模式下，抛异常会自动重试（根据配置）
        }
    }
}