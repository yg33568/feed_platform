package com.feed.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

@Data
@TableName("message_log")
public class MessageLog {
    @TableId(type = IdType.INPUT)
    private Long id;
    private Long articleId;
    private String exchange;
    private String routingKey;
    private String messageBody;
    private String status;      // PENDING, SUCCESS, FAILED
    private Integer retryCount;
    private Date nextRetryTime;
    private Date createTime;
    private Date updateTime;
}