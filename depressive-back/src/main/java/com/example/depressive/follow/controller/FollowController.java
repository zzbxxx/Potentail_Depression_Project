package com.example.depressive.follow.controller;

import com.example.depressive.article.dto.ArticleFeedDTO;
import com.example.depressive.follow.dto.FollowRequest;
import com.example.depressive.follow.dto.FollowResponse;
import com.example.depressive.follow.dto.FollowUserDTO;
import com.example.depressive.follow.dto.IsFollowingResponse;
import com.example.depressive.follow.entity.Follow;
import com.example.depressive.follow.repository.FollowRepository;
import com.example.depressive.follow.service.FollowService;
import com.example.depressive.login.entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/follow")
public class FollowController {

    @Autowired
    private FollowService followService;

    @PostMapping("/follow")
    public FollowResponse follow(@RequestBody FollowRequest request, @RequestParam long userId) {
        return followService.follow(request,userId);
    }

    @PostMapping("/unfollow")
    public FollowResponse unfollow(@RequestBody FollowRequest request,@RequestParam long userId) {
        return followService.unfollow(request,userId);
    }

    @GetMapping("/isFollowing")
    public IsFollowingResponse isFollowing(@RequestParam Long followerId, @RequestParam Long followedId) {
        return followService.isFollowing(followerId, followedId);
    }

    @GetMapping("/following")
    public List<FollowUserDTO> getFollowing(@RequestParam Long userId) {
        System.out.println("Fetching following for userId: " + userId);
        return followService.getFollowing(userId);
    }

    @GetMapping("/follower")
    public List<FollowUserDTO> getFollowers(@RequestParam Long userId) {
        System.out.println("Fetching followers for userId: " + userId);
        return followService.getFollowers(userId);
    }

    @GetMapping("/feed")
    public List<ArticleFeedDTO> getFollowingArticles( @RequestParam long userId) {
        System.out.println("uuid:"+userId);
        return followService.getFollowingArticles(userId);
    }

}