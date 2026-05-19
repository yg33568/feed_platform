package com.feed.config;

import com.feed.mapper.MessageLogMapper;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;

@Configuration
public class RabbitMQConfig {

    public static final String ARTICLE_EXCHANGE = "article.exchange";
    public static final String ARTICLE_QUEUE = "article.queue";
    public static final String ARTICLE_ROUTING_KEY = "article.create";

    @Bean
    public DirectExchange articleExchange() {
        return new DirectExchange(ARTICLE_EXCHANGE);
    }

    @Bean
    public Binding articleBinding(Queue articleQueue) {
        return BindingBuilder
                .bind(articleQueue)
                .to(articleExchange())
                .with(ARTICLE_ROUTING_KEY);
    }

    @Autowired
    private CachingConnectionFactory connectionFactory;

    @Autowired
    @Lazy
    private MessageLogMapper messageLogMapper;

    @Bean
    public RabbitTemplate rabbitTemplate() {
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);

        // 让 ReturnsCallback 生效
        rabbitTemplate.setMandatory(true);

        // 确认回调（消息是否到达交换机）
        rabbitTemplate.setConfirmCallback((correlationData, ack, cause) -> {
            if (correlationData == null) return;
            Long messageLogId = Long.valueOf(correlationData.getId());
            if (ack) {
                messageLogMapper.updateStatus(messageLogId, "SUCCESS");
                System.out.println("MQ发送成功: " + correlationData);
            } else {
                messageLogMapper.updateStatus(messageLogId, "FAILED");
                System.err.println("MQ发送失败: " + cause);
            }
        });

        // 返回回调（消息是否到达队列）
        rabbitTemplate.setReturnsCallback(returned -> {
            System.err.println("消息路由失败: " + returned.getMessage());
        });

        return rabbitTemplate;
    }
}