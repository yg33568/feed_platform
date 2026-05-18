package com.feed.startup;

import com.feed.mapper.UserMapper;
import com.feed.util.BloomFilterUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class BloomFilterInitializer implements CommandLineRunner {

    @Autowired
    private BloomFilterUtil bloomFilterUtil;

    @Autowired
    private UserMapper userMapper;

    @Override
    public void run(String... args) {
        // 把所有现有用户ID加入布隆过滤器
        userMapper.selectList(null).forEach(user -> {
            bloomFilterUtil.addUser(user.getId());
        });
        System.out.println("布隆过滤器初始化完成");
    }
}
