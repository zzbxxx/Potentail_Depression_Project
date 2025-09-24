package com.example.depressive.article.dto;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.Set;

@Data
public class ArticleFeedDTO {
    private Long id;
    private String title;
    private String articleType;
    private Set<String> topics;
    private String status;
    private LocalDateTime createdAt;
    private String nickname;
    private String avatar;
    private Long userId;
}