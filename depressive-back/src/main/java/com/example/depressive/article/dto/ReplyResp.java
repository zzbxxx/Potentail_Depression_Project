package com.example.depressive.article.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ReplyResp {

    private Long id;
    private Long articleId;
    private Long userId;
    private String nickname;
    private String avatar;
    private String email;
    private Long parentId;
    private String content;
    private LocalDateTime createdAt;
    private Integer likeCount;
}
