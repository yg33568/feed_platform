package com.feed.config;

import org.springframework.amqp.core.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    // 交换机名称
    public static final String ARTICLE_EXCHANGE = "article.exchange";

    // 队列名称
    public static final String ARTICLE_QUEUE = "article.queue";

    // 路由Key
    public static final String ARTICLE_ROUTING_KEY = "article.create";

    @Bean
    public DirectExchange articleExchange() {
        return new DirectExchange(ARTICLE_EXCHANGE);
    }

    @Bean
    public Queue articleQueue() {
        return new Queue(ARTICLE_QUEUE, true);
    }

    @Bean
    public Binding articleBinding() {
        return BindingBuilder
                .bind(articleQueue())
                .to(articleExchange())
                .with(ARTICLE_ROUTING_KEY);
    }
}