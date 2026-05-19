package com.feed.config;

import org.springframework.amqp.core.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DeadLetterConfig {

    // 死信交换机
    public static final String DLX_EXCHANGE = "dlx.exchange";
    public static final String DLX_QUEUE = "dlx.queue";
    public static final String DLX_ROUTING_KEY = "dlx.routingkey";

    @Bean
    public DirectExchange dlxExchange() {
        return new DirectExchange(DLX_EXCHANGE);
    }

    @Bean
    public Queue dlxQueue() {
        return QueueBuilder.durable(DLX_QUEUE).build();
    }

    @Bean
    public Binding dlxBinding() {
        return BindingBuilder.bind(dlxQueue())
                .to(dlxExchange())
                .with(DLX_ROUTING_KEY);
    }

    // 主队列：绑定死信交换机
    @Bean
    public Queue articleQueue() {
        return QueueBuilder.durable(RabbitMQConfig.ARTICLE_QUEUE)
                .deadLetterExchange(DLX_EXCHANGE)
                .deadLetterRoutingKey(DLX_ROUTING_KEY)
                .ttl(30000)  // 30秒没消费进入死信
                .build();
    }
}