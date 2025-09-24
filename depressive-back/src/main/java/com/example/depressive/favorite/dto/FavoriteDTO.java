package com.example.depressive.favorite.dto;

import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;

@Getter
@Setter
public class FavoriteDTO {
    private Long id;
    private Long userId;
    private Long favoriteableId;
    private String favoriteableType;
    private String category;
    private LocalDateTime createdAt;
    private Boolean isPrivate;
}