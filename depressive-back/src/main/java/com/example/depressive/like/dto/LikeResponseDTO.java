package com.example.depressive.like.dto;

import com.example.depressive.like.entity.Like;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class LikeResponseDTO {
    private Long id;
    private Long userId;
    private String username;
    private String nickname; // 可选，显示用户昵称
    private Long likeableId;
    private String likeableType;
    private String category;
    private LocalDateTime createdAt;

    // 构造方法，方便从 Like 实体转换
    public LikeResponseDTO(Like like) {
        this.id = like.getId();
        this.userId = like.getUser().getId();
        this.username = like.getUser().getUsername();
        this.nickname = like.getUser().getNickname();
        this.likeableId = like.getLikeableId();
        this.likeableType = like.getLikeableType();
        this.category = like.getCategory();
        this.createdAt = like.getCreatedAt();
    }
}