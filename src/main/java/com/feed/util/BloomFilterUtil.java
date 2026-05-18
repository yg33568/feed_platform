package com.feed.util;

import org.redisson.api.RBloomFilter;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

@Component
public class BloomFilterUtil {

    @Autowired
    private RedissonClient redissonClient;

    private RBloomFilter<Long> userBloomFilter;
    private RBloomFilter<Long> articleBloomFilter;

    // 布隆过滤器名称
    private static final String USER_BLOOM_NAME = "bloom:user";
    private static final String ARTICLE_BLOOM_NAME = "bloom:article";

    @PostConstruct
    public void init() {
        // 初始化用户布隆过滤器（预计容量10万，误判率1%）
        userBloomFilter = redissonClient.getBloomFilter(USER_BLOOM_NAME);
        userBloomFilter.tryInit(100000L, 0.01);

        // 初始化文章布隆过滤器
        articleBloomFilter = redissonClient.getBloomFilter(ARTICLE_BLOOM_NAME);
        articleBloomFilter.tryInit(100000L, 0.01);
    }

    /**
     * 添加用户ID到布隆过滤器
     */
    public void addUser(Long userId) {
        if (userId != null) {
            userBloomFilter.add(userId);
        }
    }

    /**
     * 检查用户ID是否可能存在
     * @return true=可能存在，false=一定不存在
     */
    public boolean mightContainUser(Long userId) {
        if (userId == null) {
            return false;
        }
        return userBloomFilter.contains(userId);
    }

    /**
     * 添加文章ID到布隆过滤器
     */
    public void addArticle(Long articleId) {
        if (articleId != null) {
            articleBloomFilter.add(articleId);
        }
    }

    /**
     * 检查文章ID是否可能存在
     */
    public boolean mightContainArticle(Long articleId) {
        if (articleId == null) {
            return false;
        }
        return articleBloomFilter.contains(articleId);
    }
}