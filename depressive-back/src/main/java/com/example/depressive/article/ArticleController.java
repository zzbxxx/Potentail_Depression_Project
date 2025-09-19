package com.example.depressive.article;

import com.example.depressive.article.dto.ArticleReq;
import com.example.depressive.article.dto.ArticleResp;

import com.example.depressive.article.entity.Article;
import com.example.depressive.article.service.ArticleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/articles")
@CrossOrigin(origins = "http://localhost:5173")
public class ArticleController {

    @Autowired
    private ArticleService articleService;

    @PostMapping("/putArticleData")
    public ResponseEntity<Article> createArticle(
            @RequestPart("article") ArticleReq articleDTO,
            @RequestPart(value = "images", required = false) MultipartFile[] images) throws IOException {
        System.out.println("Received articleDTO: " + articleDTO);
        System.out.println("Received images: " + (images != null ? images.length : 0));
        Article savedArticle = articleService.createArticle(articleDTO, images);
        return ResponseEntity.ok(savedArticle);
    }

    @GetMapping("/getAllArticles")
    public ResponseEntity<List<ArticleResp>> getAllArticles() throws IOException {
        List<ArticleResp> articles = articleService.getAllArticles();
        return ResponseEntity.ok(articles);
    }
}