package com.example.depressive.favorite.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class FavoriteStatsDTO {
    private Long totalCount;
    private Long articleCount;
    private Long topicCount;
}