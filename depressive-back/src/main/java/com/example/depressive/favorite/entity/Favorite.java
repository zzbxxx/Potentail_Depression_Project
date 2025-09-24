package com.example.depressive.favorite.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;

@Entity
@Table(name = "favorites")
@Getter
@Setter
public class Favorite {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "favoriteable_id", nullable = false)
    private Long favoriteableId;

    @Column(name = "favoriteable_type", nullable = false, length = 50)
    private String favoriteableType;

    @Column(name = "category")
    private String category;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "is_private")
    private Boolean isPrivate = true;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}