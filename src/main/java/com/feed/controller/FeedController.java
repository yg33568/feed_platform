package com.feed.controller;

import com.feed.common.Result;
import com.feed.entity.Article;
import com.feed.service.FeedService;
import com.feed.service.FollowService;
import com.feed.util.RateLimiterUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/feed")
public class FeedController {
     @Autowired
    private FeedService feedService;
     @Autowired
     private RateLimiterUtil rateLimiterUtil;
    @Autowired
    private FollowService followService;

    @Value("${feed.pull.threshold:10000}")
    private long pullThreshold;

     @GetMapping
    private Result<List<Article>> getfeed(@RequestParam Long userId, @RequestParam(defaultValue = "1") int page, @RequestParam(defaultValue = "10") int size){
         if(!rateLimiterUtil.allowRequest(userId.toString(),5,10)){
             return Result.error(429,"请求太频繁，请稍后再试");
         }

         // 2. 根据用户活跃度自动选择推/拉模式
         List<Article> articles = feedService.selectMode(userId, page, size);

         return Result.success(articles);
     }
}
