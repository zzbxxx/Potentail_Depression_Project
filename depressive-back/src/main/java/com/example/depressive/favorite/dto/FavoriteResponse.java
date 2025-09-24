package com.example.depressive.favorite.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class FavoriteResponse {
    private int code;
    private String message;
    private FavoriteDTO favorite;

    public FavoriteResponse() {}

    public FavoriteResponse(int code, String message) {
        this.code = code;
        this.message = message;
    }

    public FavoriteResponse(int code, String message, FavoriteDTO favorite) {
        this.code = code;
        this.message = message;
        this.favorite = favorite;
    }
}