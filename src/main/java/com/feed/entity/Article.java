package com.feed.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

@Data
@TableName("Article")
public class Article {
    @TableId(type = IdType.INPUT)
    private Long id;
    private Long authorId;
    private String title;       // 标题
    private String content;     // 内容
    private Integer status;     // 1=正常 2=删除
    private Date createTime;
    private Date updateTime;
}
