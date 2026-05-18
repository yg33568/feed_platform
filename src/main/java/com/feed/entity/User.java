package com.feed.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

@Data
@TableName("user")
public class User {
    @TableId(type = IdType.INPUT)  // 手动用雪花算法，不自增
    private Long id;
    private String name;
    private Integer fanCount;      // 粉丝数
    private Integer followCount;   // 关注数
    private Integer status;
    private Date createTime;
    private Integer isBigV;
    private Date lastLoginTime;  //最后一次登录时间，用于判断是否活跃
}