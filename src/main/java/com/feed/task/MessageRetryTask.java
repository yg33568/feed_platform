package com.feed.task;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.feed.entity.MessageLog;
import com.feed.mapper.MessageLogMapper;
import lombok.Data;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.List;

@Component
@EnableScheduling
public class MessageRetryTask {
    @Autowired
    private MessageLogMapper messageLogMapper;
    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Scheduled(fixedDelay = 30000)
    public void retryPendingMessages(){
        List<MessageLog> pendingList = messageLogMapper.selectList(
                new LambdaQueryWrapper<MessageLog>()
                        .eq(MessageLog::getStatus,"PENDING")
                        .le(MessageLog::getNextRetryTime, new Date())
                        .lt(MessageLog::getRetryCount,5)
        );

        for(MessageLog log:pendingList){
            CorrelationData correlationData=new CorrelationData(log.getId().toString());
            rabbitTemplate.convertAndSend(
                    log.getExchange(),
                    log.getRoutingKey(),
                    Long.valueOf(log.getMessageBody()),
                    correlationData
            );

            int nextRetryCount = log.getRetryCount() + 1;
            long delaySeconds = (long) Math.pow(2, nextRetryCount) * 10;
            Date nextRetryTime = new Date(System.currentTimeMillis() + delaySeconds * 1000);
            messageLogMapper.updateRetry(log.getId(),  nextRetryTime);
        }
    }
}
