package com.example.depressive.favorite.service;

import com.example.depressive.article.entity.Article;
import com.example.depressive.article.entity.Topic;
import com.example.depressive.article.repository.ArticleRepository;
import com.example.depressive.article.repository.TopicRepository;
import com.example.depressive.favorite.dto.*;
import com.example.depressive.favorite.entity.Favorite;
import com.example.depressive.favorite.repository.FavoriteRepository;
import com.example.depressive.login.entity.User;
import com.example.depressive.login.repository.UserRepository;
import com.example.depressive.mood.entity.CardsContent;
import com.example.depressive.mood.entity.UserCardsLog;
import com.example.depressive.mood.repository.CardsContentRepository;
import com.example.depressive.mood.repository.UserCardsLogRepository;
import com.example.depressive.notification.service.NotificationService; // 新增依賴
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.scheduling.annotation.Async; // 新增 Async 支援
import org.springframework.transaction.annotation.Propagation;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class FavoriteService {

    private final FavoriteRepository favoriteRepository;
    private final ArticleRepository articleRepository;
    private final TopicRepository topicRepository;
    private final CardsContentRepository cardsContentRepository;
    private final UserCardsLogRepository userCardsLogRepository;
    private final NotificationService notificationService; // 新增依賴
    private final UserRepository userRepository;

    private static final String TYPE_ARTICLE = "ARTICLE";
    private static final String TYPE_TOPIC = "TOPIC";
    private static final String TYPE_CARD = "CARD";

    @Transactional
    public FavoriteResponse addFavorite(FavoriteRequest request, Long userId) {
        try {
            // 验证收藏对象是否存在
            if (!validateFavoriteableObject(request.getFavoriteableId(), request.getFavoriteableType())) {
                return new FavoriteResponse(1, "收藏对象不存在");
            }

            // 检查是否已收藏
            Optional<Favorite> existingFavorite = favoriteRepository
                    .findByUserIdAndFavoriteableIdAndFavoriteableType(userId,
                            request.getFavoriteableId(), request.getFavoriteableType());

            if (existingFavorite.isPresent()) {
                return new FavoriteResponse(1, "已收藏该内容");
            }

            // 创建收藏记录
            Favorite favorite = new Favorite();
            favorite.setUserId(userId);
            favorite.setFavoriteableId(request.getFavoriteableId());
            favorite.setFavoriteableType(request.getFavoriteableType().toUpperCase());
            favorite.setCategory(request.getCategory());
            favorite.setIsPrivate(request.getIsPrivate() != null ? request.getIsPrivate() : true);

            // 如果是 CARD 类型，查询 user_cards_log 表获取最新的 ucl_id
            if (TYPE_CARD.equalsIgnoreCase(request.getFavoriteableType())) {
                Optional<UserCardsLog> userCardLog = userCardsLogRepository.findTopByUserIdAndContentIdOrderByDateDesc(
                        userId, request.getFavoriteableId().shortValue());
                if (userCardLog.isEmpty()) {
                    return new FavoriteResponse(1, "未找到对应的用户卡片记录");
                }
                favorite.setUclId(userCardLog.get().getId());
            }

            Favorite savedFavorite = favoriteRepository.save(favorite);
            log.info("用户 {} 收藏了 {} ID: {}, UCL ID: {}", userId, request.getFavoriteableType(),
                    request.getFavoriteableId(), favorite.getUclId());

            // 如果是文章，异步发送通知
            if (TYPE_ARTICLE.equalsIgnoreCase(request.getFavoriteableType())) {
                Article article = articleRepository.findById(request.getFavoriteableId())
                        .orElseThrow(() -> new RuntimeException("文章不存在"));
                Long authorId = article.getUser().getId();
                asyncCreateNotificationForFavorite(userId, request.getFavoriteableId(), authorId);
            }

            FavoriteDTO favoriteDTO = convertToDTO(savedFavorite);
            return new FavoriteResponse(0, "收藏成功", favoriteDTO);
        } catch (Exception e) {
            log.error("收藏失败 - 用户ID: {}, 对象ID: {}, 类型: {}",
                    userId, request.getFavoriteableId(), request.getFavoriteableType(), e);
            return new FavoriteResponse(1, "收藏失败，请稍后重试");
        }
    }

    @Async
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void asyncCreateNotificationForFavorite(Long userId, Long favoriteableId, Long authorId) {
        log.debug("Creating notification for favorite: userId={}, favoriteableId={}", userId, favoriteableId);

        // 如果 userId 等於 authorId，跳過通知創建
        if (userId.equals(authorId)) {
            log.debug("Skipping notification creation: userId {} is the same as authorId {}", userId, authorId);
            return;
        }

        Optional<Article> articleOptional = articleRepository.findById(favoriteableId);
        if (articleOptional.isEmpty()) {
            log.error("文章不存在: favoriteableId={}", favoriteableId);
            return;
        }
        Article article = articleOptional.get();

        Optional<User> userOptional = userRepository.findById(userId);
        if (userOptional.isEmpty()) {
            log.error("用戶不存在: userId={}", userId);
            return;
        }
        User user = userOptional.get();

        String title = "用戶 " + user.getUsername() + " 收藏了你的文章";
        String content = "你的文章《" + article.getTitle() + "》被收藏";
        notificationService.createNotification(
                authorId,
                title,
                content,
                "FAVORITE_ARTICLE",
                userId
        );
        log.debug("Notification created successfully for favorite");
    }

    @Transactional
    public FavoriteResponse removeFavorite(FavoriteRequest request, Long userId) {
        try {
            Optional<Favorite> existingFavorite = favoriteRepository
                    .findByUserIdAndFavoriteableIdAndFavoriteableType(userId,
                            request.getFavoriteableId(), request.getFavoriteableType());

            if (existingFavorite.isEmpty()) {
                return new FavoriteResponse(1, "未收藏该内容");
            }

            favoriteRepository.deleteByUserIdAndFavoriteableIdAndFavoriteableType(
                    userId, request.getFavoriteableId(), request.getFavoriteableType());

            log.info("用户 {} 取消收藏了 {} ID: {}", userId, request.getFavoriteableType(), request.getFavoriteableId());
            return new FavoriteResponse(0, "取消收藏成功");
        } catch (Exception e) {
            log.error("取消收藏失败 - 用户ID: {}, 对象ID: {}, 类型: {}",
                    userId, request.getFavoriteableId(), request.getFavoriteableType(), e);
            return new FavoriteResponse(1, "取消收藏失败，请稍后重试");
        }
    }

    public List<FavoriteItemDTO> getUserFavorites(Long userId, String type, String category) {
        try {
            List<Favorite> favorites;

            if (type != null && category != null) {
                favorites = favoriteRepository.findByUserIdAndFavoriteableTypeAndCategoryOrderByCreatedAtDesc(
                        userId, type.toUpperCase(), category);
            } else if (type != null) {
                favorites = favoriteRepository.findByUserIdAndFavoriteableTypeOrderByCreatedAtDesc(
                        userId, type.toUpperCase());
            } else if (category != null) {
                favorites = favoriteRepository.findByUserIdAndCategoryOrderByCreatedAtDesc(userId, category);
            } else {
                favorites = favoriteRepository.findByUserIdOrderByCreatedAtDesc(userId);
            }

            return favorites.stream()
                    .map(this::convertToItemDTO)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("获取用户收藏列表失败 - 用户ID: {}", userId, e);
            return List.of();
        }
    }

    public FavoriteStatsDTO getFavoriteStats(Long userId) {
        try {
            Long totalCount = favoriteRepository.countByUserId(userId);
            Long articleCount = favoriteRepository.countByUserIdAndFavoriteableType(userId, TYPE_ARTICLE);
            Long topicCount = favoriteRepository.countByUserIdAndFavoriteableType(userId, TYPE_TOPIC);
            Long cardCount = favoriteRepository.countByUserIdAndFavoriteableType(userId, TYPE_CARD);

            return new FavoriteStatsDTO(totalCount, articleCount, topicCount);
        } catch (Exception e) {
            log.error("获取收藏统计失败 - 用户ID: {}", userId, e);
            return new FavoriteStatsDTO(0L, 0L, 0L);
        }
    }

    public Boolean isFavorited(Long userId, Long favoriteableId, String favoriteableType) {
        try {
            return favoriteRepository.existsByUserIdAndFavoriteableIdAndFavoriteableType(
                    userId, favoriteableId, favoriteableType);
        } catch (Exception e) {
            log.error("检查收藏状态失败 - 用户ID: {}, 对象ID: {}, 类型: {}",
                    userId, favoriteableId, favoriteableType, e);
            return false;
        }
    }

    private boolean validateFavoriteableObject(Long favoriteableId, String favoriteableType) {
        if (favoriteableId == null || favoriteableType == null) {
            return false;
        }

        return switch (favoriteableType.toUpperCase()) {
            case TYPE_ARTICLE -> articleRepository.existsById(favoriteableId);
            case TYPE_TOPIC -> topicRepository.existsById(favoriteableId);
            case TYPE_CARD -> cardsContentRepository.existsById(favoriteableId.shortValue());
            default -> false;
        };
    }

    private FavoriteDTO convertToDTO(Favorite favorite) {
        FavoriteDTO dto = new FavoriteDTO();
        dto.setId(favorite.getId());
        dto.setUserId(favorite.getUserId());
        dto.setFavoriteableId(favorite.getFavoriteableId());
        dto.setFavoriteableType(favorite.getFavoriteableType());
        dto.setCategory(favorite.getCategory());
        dto.setCreatedAt(favorite.getCreatedAt());
        dto.setIsPrivate(favorite.getIsPrivate());
        dto.setUclId(favorite.getUclId());
        return dto;
    }

    private FavoriteItemDTO convertToItemDTO(Favorite favorite) {
        FavoriteItemDTO dto = new FavoriteItemDTO();
        dto.setFavoriteId(favorite.getId());
        dto.setFavoriteableId(favorite.getFavoriteableId());
        dto.setFavoriteableType(favorite.getFavoriteableType());
        dto.setCategory(favorite.getCategory());
        dto.setCreatedAt(favorite.getCreatedAt());

        switch (favorite.getFavoriteableType()) {
            case TYPE_ARTICLE -> {
                Optional<Article> article = articleRepository.findById(favorite.getFavoriteableId());
                article.ifPresent(a -> {
                    dto.setTitle(a.getTitle());
                    String content = a.getContent() != null ? a.getContent().toString() : "";
                    dto.setContentPreview(content.substring(0, Math.min(100, content.length())));
                    if (a.getUser() != null) {
                        dto.setAuthorNickname(a.getUser().getNickname());
                        dto.setAuthorAvatar(a.getUser().getAvatar());
                    }
                });
            }
            case TYPE_TOPIC -> {
                Optional<Topic> topic = topicRepository.findById(favorite.getFavoriteableId());
                topic.ifPresent(t -> dto.setTitle(t.getName()));
            }
            case TYPE_CARD -> {
                Optional<CardsContent> card = cardsContentRepository.findById(favorite.getFavoriteableId().shortValue());
                card.ifPresent(c -> {
                    dto.setTitle(c.getQuoteText());
                    dto.setContentPreview(c.getBookTitle() != null ? c.getBookTitle() : "");
                    dto.setAuthorNickname(c.getAuthor() != null ? c.getAuthor() : "未知作者");
                    dto.setAuthorAvatar(null);
                });
            }
        }

        return dto;
    }
}