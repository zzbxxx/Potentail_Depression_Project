package com.example.depressive.article.entity;

import com.example.depressive.login.entity.User;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.Set;

@Entity
@Table(name = "articles")
@Getter
@Setter
@NoArgsConstructor
public class Article {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "title", nullable = false, length = 255)
    private String title;

    @Enumerated(EnumType.STRING)
    @Column(name = "article_type", nullable = false)
    private ArticleType articleType;

    @Column(name = "content", nullable = false, columnDefinition = "JSON")
    private String content;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private ArticleStatus status = ArticleStatus.DRAFT;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt = LocalDateTime.now();

    @ManyToMany
    @JoinTable(
            name = "article_topics",
            joinColumns = @JoinColumn(name = "article_id"),
            inverseJoinColumns = @JoinColumn(name = "topic_id")
    )
    private Set<Topic> topics;

    // JSON 序列化/反序列化方法
    private static final ObjectMapper objectMapper = new ObjectMapper();

    // 獲取 content 為 JSON 對象
    public Object getContentAsJson() throws JsonProcessingException {
        return objectMapper.readValue(content, Object.class);
    }

    // 設置 content 為 JSON 字符串
    public void setContentFromJson(Object content) throws JsonProcessingException {
        this.content = objectMapper.writeValueAsString(content);
    }

    public enum ArticleType {
        NEWS, BLOG, TUTORIAL, REVIEW
    }

    public enum ArticleStatus {
        DRAFT, PUBLISHED
    }
}