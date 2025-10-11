package com.example.depressive.like.entity;

import com.example.depressive.login.entity.User;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Table(name = "likes")
@Data
public class Like {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    @JsonManagedReference
    private User user; // 点赞用户

    @Column(name = "likeable_id", nullable = false)
    private Long likeableId; // 被点赞对象 ID

    @Column(name = "likeable_type", nullable = false)
    private String likeableType; // 被点赞对象类型（如 ARTICLE, ARTICLE_REPLY）

    @Column(name = "category")
    private String category; // 点赞分类（如 LIKE, SUPER_LIKE）

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now(); // 创建时间
}