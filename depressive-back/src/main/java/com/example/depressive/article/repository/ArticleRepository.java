package com.example.depressive.article.repository;

import com.example.depressive.article.entity.Article;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ArticleRepository extends JpaRepository<Article, Long> {
    List<Article> findByStatus(Article.ArticleStatus status);

    List<Article> findByUserIdInAndStatus(List<Long> userIds, Article.ArticleStatus status);
}