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
    private String nickname; // 用戶暱稱
    private String avatar;   // 用戶頭像
    private Long userId;    // 用戶 ID

    @Data
    public static class BlockDTO {
        private String type;
        private String content;
    }
}