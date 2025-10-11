package com.example.depressive.like.service;

import com.example.depressive.article.entity.Article;
import com.example.depressive.article.repository.ArticleRepository;
import com.example.depressive.like.dto.LikeResponseDTO;
import com.example.depressive.like.dto.LikeStatusDTO;
import com.example.depressive.like.entity.Like;
import com.example.depressive.like.repository.LikeRepository;
import com.example.depressive.login.entity.User;
import com.example.depressive.login.repository.UserRepository;
import com.example.depressive.notification.service.NotificationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.annotation.Propagation;

import java.time.LocalDateTime;

@Service
public class LikeService {

    private static final Logger log = LoggerFactory.getLogger(LikeService.class);

    @Autowired
    private LikeRepository likeRepository;

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ArticleRepository articleRepository;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    public LikeResponseDTO createLike(Long userId, Long likeableId, String likeableType, String category) {
        String setKey = "likes:" + likeableType + ":" + likeableId;
        String countKey = "like_count:" + likeableType + ":" + likeableId;

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("用戶不存在"));

        Long authorId = null;
        if ("ARTICLE".equals(likeableType)) {
            Article article = articleRepository.findById(likeableId)
                    .orElseThrow(() -> new RuntimeException("文章不存在"));
            authorId = article.getUser().getId(); // 獲取文章作者 ID
        }

        if (Boolean.TRUE.equals(redisTemplate.opsForSet().isMember(setKey, userId.toString()))) {
            throw new RuntimeException("已點讚");
        }

        Like like = new Like();
        like.setUser(user);
        like.setLikeableId(likeableId);
        like.setLikeableType(likeableType);
        like.setCategory(category != null ? category : "LIKE");
        like.setCreatedAt(LocalDateTime.now());

        try {
            redisTemplate.opsForSet().add(setKey, userId.toString());
            redisTemplate.opsForValue().increment(countKey);
            asyncSaveToDatabase(like);
            // 傳遞 authorId 給通知方法
            asyncCreateNotificationForLike(userId, likeableId, likeableType, authorId);
        } catch (Exception e) {
            redisTemplate.opsForSet().remove(setKey, userId.toString());
            redisTemplate.opsForValue().decrement(countKey);
            throw new RuntimeException("點讚失敗: " + e.getMessage(), e);
        }

        return new LikeResponseDTO(like);
    }

    public void unlike(Long userId, Long likeableId, String likeableType) {
        String setKey = "likes:" + likeableType + ":" + likeableId;
        String countKey = "like_count:" + likeableType + ":" + likeableId;

        if (!Boolean.TRUE.equals(redisTemplate.opsForSet().isMember(setKey, userId.toString()))) {
            throw new RuntimeException("未點讚");
        }

        try {
            redisTemplate.opsForSet().remove(setKey, userId.toString());
            redisTemplate.opsForValue().decrement(countKey);
            asyncDeleteFromDatabase(userId, likeableId, likeableType);
        } catch (Exception e) {
            redisTemplate.opsForSet().add(setKey, userId.toString());
            redisTemplate.opsForValue().increment(countKey);
            throw new RuntimeException("取消點讚失敗: " + e.getMessage(), e);
        }
    }

    public LikeStatusDTO isLiked(Long userId, Long likeableId, String likeableType) {
        String setKey = "likes:" + likeableType + ":" + likeableId;
        String countKey = "like_count:" + likeableType + ":" + likeableId;
        boolean isLiked = Boolean.TRUE.equals(redisTemplate.opsForSet().isMember(setKey, userId.toString()))
                || likeRepository.existsByUserIdAndLikeableIdAndLikeableType(userId, likeableId, likeableType);
        Long likeCount = redisTemplate.opsForValue().get(countKey) != null
                ? Long.valueOf(redisTemplate.opsForValue().get(countKey).toString()) : 0L;
        return new LikeStatusDTO(isLiked, likeCount);
    }

    @Async
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void asyncSaveToDatabase(Like like) {
        log.debug("Saving like to database: userId={}, likeableId={}, likeableType={}",
                like.getUser().getId(), like.getLikeableId(), like.getLikeableType());
        likeRepository.save(like);
        log.debug("Like saved successfully");
    }

    @Async
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void asyncDeleteFromDatabase(Long userId, Long likeableId, String likeableType) {
        log.debug("Deleting like from database: userId={}, likeableId={}, likeableType={}",
                userId, likeableId, likeableType);
        likeRepository.deleteByUserIdAndLikeableIdAndLikeableType(userId, likeableId, likeableType);
        log.debug("Like deleted successfully");
    }

    @Async
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void asyncCreateNotificationForLike(Long userId, Long likeableId, String likeableType, Long authorId) {
        log.debug("Creating notification: userId={}, likeableId={}, likeableType={}",
                userId, likeableId, likeableType);

        // 檢查 userId 是否等於 authorId
        if (userId.equals(authorId)) {
            log.debug("Skipping notification creation: userId {} is the same as authorId {}", userId, authorId);
            return;
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("用戶不存在"));

        if ("ARTICLE".equals(likeableType)) {
            Article article = articleRepository.findById(likeableId)
                    .orElseThrow(() -> new RuntimeException("文章不存在"));
            String title = "用戶 " + user.getUsername() + " 點讚了你的文章";
            String content = "你的文章《" + article.getTitle() + "》獲得了點讚";
            notificationService.createNotification(
                    article.getUser().getId(),
                    title,
                    content,
                    "LIKE_ARTICLE",
                    userId
            );
        }
        log.debug("Notification created successfully");
    }
}