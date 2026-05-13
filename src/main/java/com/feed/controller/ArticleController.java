package com.feed.controller;

import com.feed.mapper.ArticleMapper;
import com.feed.service.ArticleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/article")
public class ArticleController {
    @Autowired
    private ArticleService articleService;

    @PostMapping("/publish")
    public String publish(@RequestParam Long authorId,@RequestParam String title, @RequestParam String content){
        Long articleId = articleService.publishArticle(authorId, title, content);
        return "文章发布成功，ID: " + articleId;
    }
}
