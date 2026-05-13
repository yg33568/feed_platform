package com.feed.service;

import com.feed.entity.Article;
import com.feed.mapper.ArticleMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Service
public class FeedService {

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;
    @Autowired
    private ArticleMapper articleMapper;

    public List<Article> getFeed(Long userId, int page, int size) {
        int start = (page - 1) * size;
        int end = start + size - 1;

        Set<Object> articleIds = redisTemplate.opsForZSet()
                .reverseRange("inbox:" + userId, start, end);

        if (articleIds == null || articleIds.isEmpty()) {
            return new ArrayList<>();
        }

        List<Long> ids = new ArrayList<>();
        for (Object id : articleIds) {
            ids.add(Long.valueOf(id.toString()));
        }

        return articleMapper.selectBatchIds(ids);
    }

}
