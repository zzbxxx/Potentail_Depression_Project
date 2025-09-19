package com.example.depressive.article.dto;

import lombok.Data;

import java.util.List;

@Data
public class ArticleReq {
    private long userId;
    private String title;
    private String articleType;
    private List<String> topics;
    private List<BlockDTO> blocks;

    @Data
    public static class BlockDTO {
        private String type;
        private String content; // 文本或圖片 blob URL
    }
}
