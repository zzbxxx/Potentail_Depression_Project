package com.example.depressive.favorite.dto;

import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;

@Getter
@Setter
public class FavoriteItemDTO {
    private Long favoriteId;
    private Long favoriteableId;
    private String favoriteableType;
    private String title;
    private String contentPreview;
    private String category;
    private LocalDateTime createdAt;
    private Object extraInfo;

    private Long authorId;
    private String authorNickname;
    private String authorAvatar;
}