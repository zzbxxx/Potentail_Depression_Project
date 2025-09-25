package com.example.depressive.follow.service;

import com.example.depressive.article.dto.ArticleFeedDTO;
import com.example.depressive.article.entity.Article;
import com.example.depressive.article.repository.ArticleRepository;
import com.example.depressive.follow.dto.FollowRequest;
import com.example.depressive.follow.dto.FollowResponse;
import com.example.depressive.follow.entity.Follow;
import com.example.depressive.follow.repository.FollowRepository;
import com.example.depressive.login.entity.User;
import com.example.depressive.login.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
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
    public FollowResponse follow(FollowRequest request, long userId) {
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

    public List<Long> getFollowing(Long userId) {
        return followRepository.findByFollowerId(userId)
                .stream()
                .map(Follow::getFollowedId)
                .collect(Collectors.toList());
    }

    public List<Long> getFollowers(Long userId) {
        return followRepository.findByFollowedId(userId)
                .stream()
                .map(Follow::getFollowerId)
                .collect(Collectors.toList());
    }

    public List<ArticleFeedDTO> getFollowingArticles(Long userId) {

        List<Long> followingIds = getFollowing(userId);
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