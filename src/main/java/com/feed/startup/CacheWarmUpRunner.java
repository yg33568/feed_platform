package com.feed.startup;

import com.feed.service.CacheWarmUpService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class CacheWarmUpRunner implements CommandLineRunner {

    @Autowired
    private CacheWarmUpService cacheWarmUpService;

    @Override
    public void run(String... args) throws Exception {
        System.out.println("===== 开始缓存预热 =====");
        // 粉丝数超过 10000 视为大V
        cacheWarmUpService.warmUpBigVInbox(10000);
        System.out.println("===== 缓存预热完成 =====");
    }
}