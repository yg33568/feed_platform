package com.feed.mq;

import com.feed.entity.Article;
import com.feed.mapper.ArticleMapper;
import com.feed.config.DeadLetterConfig;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class DeadLetterConsumer {

    @Autowired
    private ArticleMapper articleMapper;

    @RabbitListener(queues = DeadLetterConfig.DLX_QUEUE)
    public void handleDeadLetter(Long articleId) {
        System.err.println("【告警】消息进入死信队列，人工介入处理: articleId=" + articleId);

        // 可选：记录到数据库，发送邮件/钉钉告警
        Article article = articleMapper.selectById(articleId);
        if (article != null) {
            System.err.println("文章内容: " + article.getTitle());
        }
    }
}