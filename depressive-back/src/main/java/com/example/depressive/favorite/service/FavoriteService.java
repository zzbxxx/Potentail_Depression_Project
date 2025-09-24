package com.example.depressive.favorite.service;

import com.example.depressive.article.entity.Article;
import com.example.depressive.article.repository.ArticleRepository;
import com.example.depressive.article.repository.TopicRepository;
import com.example.depressive.favorite.dto.*;
import com.example.depressive.favorite.entity.Favorite;
import com.example.depressive.favorite.repository.FavoriteRepository;
import com.example.depressive.article.entity.Topic;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

    private static final String TYPE_ARTICLE = "ARTICLE";
    private static final String TYPE_TOPIC = "TOPIC";

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

            Favorite savedFavorite = favoriteRepository.save(favorite);
            log.info("用户 {} 收藏了 {} ID: {}", userId, request.getFavoriteableType(), request.getFavoriteableId());

            FavoriteDTO favoriteDTO = convertToDTO(savedFavorite);
            return new FavoriteResponse(0, "收藏成功", favoriteDTO);
        } catch (Exception e) {
            log.error("收藏失败 - 用户ID: {}, 对象ID: {}, 类型: {}",
                    userId, request.getFavoriteableId(), request.getFavoriteableType(), e);
            return new FavoriteResponse(1, "收藏失败，请稍后重试");
        }
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
        return dto;
    }

    private FavoriteItemDTO convertToItemDTO(Favorite favorite) {
        FavoriteItemDTO dto = new FavoriteItemDTO();
        dto.setFavoriteId(favorite.getId());
        dto.setFavoriteableId(favorite.getFavoriteableId());
        dto.setFavoriteableType(favorite.getFavoriteableType());
        dto.setCategory(favorite.getCategory());
        dto.setCreatedAt(favorite.getCreatedAt());

        // 根据类型设置额外信息
        switch (favorite.getFavoriteableType()) {
            case TYPE_ARTICLE -> {
                Optional<Article> article = articleRepository.findById(favorite.getFavoriteableId());
                article.ifPresent(a -> {
                    dto.setTitle(a.getTitle());
                    // 安全处理JSON内容
                    String content = a.getContent() != null ? a.getContent().toString() : "";
                    dto.setContentPreview(content.substring(0, Math.min(100, content.length())));
                });
            }
            case TYPE_TOPIC -> {
                Optional<Topic> topic = topicRepository.findById(favorite.getFavoriteableId());
                topic.ifPresent(t -> dto.setTitle(t.getName()));
            }
        }

        return dto;
    }
}