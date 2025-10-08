package com.example.depressive.article.entity;

import com.example.depressive.article.converter.ArticleTypeConverter;
import com.example.depressive.login.entity.User;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
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

    @Convert(converter = ArticleTypeConverter.class)
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

    @Column(name = "is_public_in_follow", nullable = false)
    private Boolean isPublicInFollow = true;

    @ManyToMany
    @JoinTable(
            name = "article_topics",
            joinColumns = @JoinColumn(name = "article_id"),
            inverseJoinColumns = @JoinColumn(name = "topic_id")
    )
    private Set<Topic> topics;

    // JSON 序列化/反序列化方法
    private static final ObjectMapper objectMapper = new ObjectMapper();

    public List<Map<String, String>> getContentAsJson() throws IOException {
        return objectMapper.readValue(content, new TypeReference<List<Map<String, String>>>() {});
    }

    public void setContentFromJson(Object content) throws JsonProcessingException {
        this.content = objectMapper.writeValueAsString(content);
    }

    public enum ArticleStatus {
        DRAFT,
        PUBLISHED,
        APPROVED,
        REJECTED
    }
}