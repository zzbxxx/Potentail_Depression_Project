package com.example.depressive.follow.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class FollowUserDTO {
    private Long id;
    private String username;
    private String nickname;
    private String avatar;
    private LocalDateTime joinDate; // 加入日期（users.created_at）
    private LocalDateTime createdAt; // 關注日期（follows.created_at）
    private Boolean isFollowing; // 是否已關注（僅用於「關注我的」列表）
}