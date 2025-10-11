package com.example.depressive.like.dto;

import lombok.Data;

@Data
public class LikeDTO {
    private Long userId;
    private Long likeableId;
    private Long authorId;
    private String likeableType;
    private String category;
}