package com.feed.controller;

import com.feed.entity.Article;
import com.feed.service.FeedService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/feed")
public class FeedController {
     @Autowired
    private FeedService feedService;

     @GetMapping
    private List<Article> getfeed(@RequestParam Long userId, @RequestParam(defaultValue = "1") int page, @RequestParam(defaultValue = "10") int size){
         return feedService.getFeed(userId, page, size);
     }
}
