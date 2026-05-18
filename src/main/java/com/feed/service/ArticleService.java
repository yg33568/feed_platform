package com.feed.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.feed.config.RabbitMQConfig;
import com.feed.entity.Article;
import com.feed.entity.Follow;
import com.feed.mapper.ArticleMapper;
import com.feed.mapper.FollowMapper;
import com.feed.util.BloomFilterUtil;
import com.feed.util.SnowflakeIdUtil;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

@Service
public class ArticleService {
    @Autowired
    private FollowMapper followMapper;
    @Autowired
    private ArticleMapper articleMapper;
    @Autowired
    private SnowflakeIdUtil snowflakeIdUtil;
    @Autowired
    private RabbitTemplate rabbitTemplate;
    @Autowired
    private BloomFilterUtil bloomFilterUtil;

    /**
     * 发文章
     */
    @Transactional
    public Long publishArticle(Long authorId, String title, String content) {
        // 布隆过滤器检查
        if (!bloomFilterUtil.mightContainUser(authorId)) {
            throw new RuntimeException("作者用户不存在");
        }
        // 1. 保存文章
        Article article=new Article();
        article.setAuthorId(authorId);
        article.setTitle(title);
        article.setContent(content);
        article.setId(snowflakeIdUtil.nextId());
        article.setStatus(1);
        article.setCreateTime(new Date());
        article.setUpdateTime(new Date());
        articleMapper.insert(article);
        // 文章保存成功后
        bloomFilterUtil.addArticle(article.getId());

        // 2. 发送MQ消息，异步推送粉丝
        rabbitTemplate.convertAndSend(
                RabbitMQConfig.ARTICLE_EXCHANGE,
                RabbitMQConfig.ARTICLE_ROUTING_KEY,
                article.getId()
        );
        return article.getId();
    }

    /**
     * 获取粉丝列表
     */
    public List<Follow> getFans(Long followId) {
        LambdaQueryWrapper<Follow> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Follow::getStatus,1)
                .eq(Follow::getFollowId,followId);
        return followMapper.selectList(wrapper);
    }
}
