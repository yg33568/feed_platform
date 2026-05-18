package com.feed.task;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.feed.entity.User;
import com.feed.mapper.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@EnableScheduling
public class BigVUpdateTask {

    @Autowired
    private UserMapper userMapper;

    // 每小时执行一次
    @Scheduled(fixedDelay = 3600000)
    public void updateBigVStatus() {
        // 1. 粉丝数 >= 10000 的设为大V
        LambdaUpdateWrapper<User> wrapperBig = new LambdaUpdateWrapper<>();
        wrapperBig.set(User::getIsBigV, 1)
                .ge(User::getFanCount, 10000);
        userMapper.update(null, wrapperBig);

        // 2. 粉丝数 < 10000 的取消大V
        LambdaUpdateWrapper<User> wrapperSmall = new LambdaUpdateWrapper<>();
        wrapperSmall.set(User::getIsBigV, 0)
                .lt(User::getFanCount, 10000);
        userMapper.update(null, wrapperSmall);

        System.out.println("大V状态更新完成");
    }
}