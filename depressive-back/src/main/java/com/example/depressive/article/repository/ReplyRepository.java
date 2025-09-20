package com.example.depressive.article.repository;

import com.example.depressive.article.entity.Reply;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ReplyRepository extends JpaRepository<Reply, Long> {
    List<Reply> findByArticleIdAndIsDeletedFalse(Long articleId);
}