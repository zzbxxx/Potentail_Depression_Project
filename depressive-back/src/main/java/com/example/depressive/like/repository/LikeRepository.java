package com.example.depressive.like.repository;

import com.example.depressive.like.entity.Like;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public interface LikeRepository extends JpaRepository<Like, Long> {
    @Transactional
    @Modifying
    @Query("DELETE FROM Like l WHERE l.user.id = :userId AND l.likeableId = :likeableId AND l.likeableType = :likeableType")
    void deleteByUserIdAndLikeableIdAndLikeableType(Long userId, Long likeableId, String likeableType);

    boolean existsByUserIdAndLikeableIdAndLikeableType(Long userId, Long likeableId, String likeableType);
}