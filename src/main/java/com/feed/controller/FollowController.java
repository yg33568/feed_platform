package com.feed.controller;

import com.feed.service.FollowService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class FollowController {
    @Autowired
    private FollowService followService;

    @GetMapping("/follow")
    public String follow(@RequestParam Long userId,@RequestParam Long followId){
        followService.follow(userId,followId);
        return "关注成功";
    }
    @GetMapping("/unfollow")
    public String unfollow(@RequestParam Long userId,@RequestParam Long followId){
        followService.unfollow(userId,followId);
        return "取关成功";
    }
}
