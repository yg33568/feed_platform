package com.feed;

import com.feed.mapper.UserMapper;
import com.feed.service.FollowService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class FeedApplicationTests {

    @Autowired
    private UserMapper userMapper;
    @Autowired
    private FollowService followService;

//    @Test
//    void testMapper() {
//        System.out.println(userMapper.selectList(null));
//    }

    @Test
    void testFollow() {
        followService.follow(1L, 2L);
        System.out.println("关注成功");
    }
}