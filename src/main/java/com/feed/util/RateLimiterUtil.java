package com.feed.util;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Component
public class RateLimiterUtil {

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    /**
     * 令牌桶限流（简化版：计数器限流）
     * @param key 限流key（例如 userId）
     * @param maxRequests 最大请求次数
     * @param timeWindow 时间窗口（秒）
     * @return true 表示放行，false 表示被限流
     */
    public boolean allowRequest(String key, int maxRequests, int timeWindow) {
        String redisKey = "rate_limit:" + key;
        Long count = redisTemplate.opsForValue().increment(redisKey);

        if (count == 1) {
            // 第一次请求，设置过期时间
            redisTemplate.expire(redisKey, timeWindow, TimeUnit.SECONDS);
        }

        return count <= maxRequests;
    }
}