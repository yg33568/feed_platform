package com.feed.mq;

import com.feed.entity.Article;
import com.feed.entity.Follow;
import com.feed.mapper.ArticleMapper;
import com.feed.service.ArticleService;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import com.feed.config.RabbitMQConfig;

@Component
public class FeedConsumer {

    @Autowired
    private ArticleService articleService;
    @Autowired
    private ArticleMapper articleMapper;
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @RabbitListener(queues = RabbitMQConfig.ARTICLE_QUEUE)
    public void handleArticleCreate(Long articleId) {
        // 1. 查询文章
        Article article = articleMapper.selectById(articleId);
        if (article == null) {
            return;
        }

        // 2. 获取作者的粉丝列表
        java.util.List<Follow> fans = articleService.getFans(article.getAuthorId());

        // 3. 推送文章到每个粉丝的收件箱（Redis ZSet）
        // key: inbox:{userId}, score: 文章发布时间戳, value: articleId
        for (Follow fan : fans) {
            String key = "inbox:" + fan.getUserId();
            redisTemplate.opsForZSet().add(key, articleId, article.getCreateTime().getTime());
        }

        System.out.println("推送文章 " + articleId + " 给 " + fans.size() + " 个粉丝");
    }
}