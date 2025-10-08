package com.example.depressive.article.dto;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

@Data
public class ArticleResp {
    private Long id;
    private String title;
    private String articleType;
    private Set<String> topics;
    private List<BlockDTO> blocks;
    private String status;
    private LocalDateTime createdAt;
    private String nickname;
    private String avatar;
    private Long userId;
    private Boolean isPublicInFollow;

    @Data
    public static class BlockDTO {
        private String type;
        private String content;
    }
}