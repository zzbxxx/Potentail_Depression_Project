package com.example.depressive.favorite.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class FavoriteRequest {
    private Long favoriteableId;
    private String favoriteableType;
    private String category;
    private Boolean isPrivate = true;
}