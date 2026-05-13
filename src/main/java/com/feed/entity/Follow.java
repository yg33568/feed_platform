package com.feed.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

@Data
@TableName("follow")
public class Follow {
    @TableId(type = IdType.INPUT)
    private Long id;
    private Long userId;        // 粉丝ID
    private Long followId;      // 关注的作者ID
    private Integer status;     // 1=关注 0=取关
    private Date createTime;
}