package com.example.depressive.article;

import com.example.depressive.article.dto.ArticleDTO;
import com.example.depressive.article.entity.Article;
import com.example.depressive.article.service.ArticleService;
import com.example.depressive.login.entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("/api/articles")
@CrossOrigin(origins = "http://localhost:5173")
public class ArticleController {

    @Autowired
    private ArticleService articleService;

    @PostMapping("/putArticleData")
    public ResponseEntity<Article> createArticle(
            @RequestPart("article") ArticleDTO articleDTO,
            @RequestPart(value = "images", required = false) MultipartFile[] images) throws IOException {
//        if (articleDTO.getUserId() == null) {
//            return ResponseEntity.badRequest().body(null); // userId 為空
//        }
        Article savedArticle = articleService.createArticle(articleDTO, images);
        return ResponseEntity.ok(savedArticle);
    }
}