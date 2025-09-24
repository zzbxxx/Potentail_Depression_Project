package com.example.depressive.follow.repository;

import com.example.depressive.follow.entity.Follow;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FollowRepository extends JpaRepository<Follow, Long> {
    // 检查是否已关注
    Optional<Follow> findByFollowerIdAndFollowedId(Long followerId, Long followedId);

    // 获取用户的关注列表
    List<Follow> findByFollowerId(Long followerId);

    // 获取用户的粉丝列表
    List<Follow> findByFollowedId(Long followedId);

    // 删除关注关系
    void deleteByFollowerIdAndFollowedId(Long followerId, Long followedId);
}