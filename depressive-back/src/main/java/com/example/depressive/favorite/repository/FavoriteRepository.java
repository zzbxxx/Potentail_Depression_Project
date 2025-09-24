package com.example.depressive.favorite.repository;

import com.example.depressive.favorite.entity.Favorite;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FavoriteRepository extends JpaRepository<Favorite, Long> {

    Optional<Favorite> findByUserIdAndFavoriteableIdAndFavoriteableType(Long userId, Long favoriteableId, String favoriteableType);

    List<Favorite> findByUserIdOrderByCreatedAtDesc(Long userId);

    List<Favorite> findByUserIdAndFavoriteableTypeOrderByCreatedAtDesc(Long userId, String favoriteableType);

    List<Favorite> findByUserIdAndCategoryOrderByCreatedAtDesc(Long userId, String category);

    List<Favorite> findByUserIdAndFavoriteableTypeAndCategoryOrderByCreatedAtDesc(Long userId, String favoriteableType, String category);

    void deleteByUserIdAndFavoriteableIdAndFavoriteableType(Long userId, Long favoriteableId, String favoriteableType);

    Long countByUserId(Long userId);

    Long countByUserIdAndFavoriteableType(Long userId, String favoriteableType);

    Boolean existsByUserIdAndFavoriteableIdAndFavoriteableType(Long userId, Long favoriteableId, String favoriteableType);
}