package com.example.depressive.follow.service;

import com.example.depressive.article.dto.ArticleFeedDTO;
import com.example.depressive.article.entity.Article;
import com.example.depressive.article.repository.ArticleRepository;
import com.example.depressive.follow.dto.FollowRequest;
import com.example.depressive.follow.dto.FollowResponse;
import com.example.depressive.follow.dto.FollowUserDTO;
import com.example.depressive.follow.dto.IsFollowingResponse;
import com.example.depressive.follow.entity.Follow;
import com.example.depressive.follow.repository.FollowRepository;
import com.example.depressive.login.entity.User;
import com.example.depressive.login.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class FollowService {

    @Autowired
    private FollowRepository followRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ArticleRepository articleRepository;


    @Transactional
    public FollowResponse follow(FollowRequest request, Long userId) {
        Long followerId = userId;
        Long followedId = request.getFollowedId();

        Optional<User> followedUser = userRepository.findById(followedId);
        if (!followedUser.isPresent()) {
            return new FollowResponse() {{
                setCode(1);
                setMessage("被关注用户不存在");
            }};
        }

        if (followerId.equals(followedId)) {
            return new FollowResponse() {{
                setCode(1);
                setMessage("不能关注自己");
            }};
        }

        Optional<Follow> existingFollow = followRepository.findByFollowerIdAndFollowedId(followerId, followedId);
        if (existingFollow.isPresent()) {
            return new FollowResponse() {{
                setCode(1);
                setMessage("已关注该用户");
            }};
        }

        Follow follow = new Follow();
        follow.setFollowerId(followerId);
        follow.setFollowedId(followedId);
        follow.setIsPrivate(true);
        followRepository.save(follow);

        return new FollowResponse() {{
            setCode(0);
            setMessage("关注成功");
        }};
    }

    @Transactional
    public FollowResponse unfollow(FollowRequest request ,long userId) {
        Long followerId = userId;
        Long followedId = request.getFollowedId();

        Optional<Follow> existingFollow = followRepository.findByFollowerIdAndFollowedId(followerId, followedId);
        if (!existingFollow.isPresent()) {
            return new FollowResponse() {{
                setCode(1);
                setMessage("未关注该用户");
            }};
        }

        followRepository.deleteByFollowerIdAndFollowedId(followerId, followedId);
        return new FollowResponse() {{
            setCode(0);
            setMessage("取消关注成功");
        }};
    }


    public IsFollowingResponse isFollowing(Long followerId, Long followedId) {
        Optional<Follow> existingFollow = followRepository.findByFollowerIdAndFollowedId(followerId, followedId);
        IsFollowingResponse response = new IsFollowingResponse();
        response.setIsFollowing(existingFollow.isPresent());
        return response;
    }

    public List<FollowUserDTO> getFollowing(Long userId) {
        List<Follow> follows = followRepository.findByFollowerId(userId);
        return follows.stream().map(follow -> {
            User user = userRepository.findById(follow.getFollowedId())
                    .orElseThrow(() -> new RuntimeException("User not found: " + follow.getFollowedId()));
            FollowUserDTO dto = new FollowUserDTO();
            dto.setId(user.getId());
            dto.setUsername(user.getUsername());
            dto.setNickname(user.getNickname());
            dto.setAvatar(user.getAvatar());
            dto.setJoinDate(user.getCreatedAt());
            dto.setCreatedAt(follow.getCreatedAt());
            dto.setIsFollowing(true); // 關注的人默認已關注
            return dto;
        }).collect(Collectors.toList());
    }

    // 修改為返回 FollowUserDTO 列表
    public List<FollowUserDTO> getFollowers(Long userId) {
        List<Follow> follows = followRepository.findByFollowedId(userId);
        return follows.stream().map(follow -> {
            User user = userRepository.findById(follow.getFollowerId())
                    .orElseThrow(() -> new RuntimeException("User not found: " + follow.getFollowerId()));
            FollowUserDTO dto = new FollowUserDTO();
            dto.setId(user.getId());
            dto.setUsername(user.getUsername());
            dto.setNickname(user.getNickname());
            dto.setAvatar(user.getAvatar());
            dto.setJoinDate(user.getCreatedAt());
            dto.setCreatedAt(follow.getCreatedAt());
            boolean isFollowing = followRepository.findByFollowerIdAndFollowedId(userId, user.getId()).isPresent();
            dto.setIsFollowing(isFollowing);
            return dto;
        }).collect(Collectors.toList());
    }
    public List<ArticleFeedDTO> getFollowingArticles(Long userId) {

        List<Long> followingIds = getFollowing(userId).stream()
                .map(FollowUserDTO::getId)
                .collect(Collectors.toList());
        return articleRepository.findByUserIdInAndStatus(followingIds, Article.ArticleStatus.PUBLISHED)
                .stream()
                .map(article -> {
                    ArticleFeedDTO dto = new ArticleFeedDTO();
                    dto.setId(article.getId());
                    dto.setTitle(article.getTitle());
                    dto.setArticleType(article.getArticleType().name());
                    dto.setTopics(article.getTopics().stream()
                            .map(topic -> topic.getName()) // 假设 Topic 实体有 getName()
                            .collect(Collectors.toSet()));
                    dto.setStatus(article.getStatus().name());
                    dto.setCreatedAt(article.getCreatedAt());
                    dto.setNickname(article.getUser().getNickname());
                    dto.setAvatar(article.getUser().getAvatar());
                    dto.setUserId(article.getUser().getId());
                    return dto;
                })
                .collect(Collectors.toList());
    }
}