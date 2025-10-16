package com.example.depressive.article;

import com.example.depressive.article.dto.ArticleReq;
import com.example.depressive.article.dto.ArticleResp;
import com.example.depressive.article.entity.Article;
import com.example.depressive.article.repository.ArticleRepository;
import com.example.depressive.article.service.ArticleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/articles")
@CrossOrigin(origins = "http://localhost:5173")
public class ArticleController {

    @Autowired
    private ArticleService articleService;


    @GetMapping("/getArticlesByUserId")
    public ResponseEntity<List<ArticleResp>> getArticlesByUserId(@RequestParam Long userId) throws IOException, IOException {

        List<ArticleResp> articles = articleService.getArticlesByUserId(userId);
        return ResponseEntity.ok(articles);
    }
    @PostMapping("/putArticleData")
    public ResponseEntity<Article> putArticleData(
                                                    @RequestPart("article") ArticleReq articleDTO,
                                                    @RequestPart(value = "images", required = false) MultipartFile[] images) throws IOException {

        // ✅ 添加 id 判断
        if (articleDTO.getId() != null) {
            return ResponseEntity.ok(articleService.updateArticle(articleDTO, images));
        } else {
            return ResponseEntity.ok(articleService.createArticle(articleDTO, images));
        }
    }

    @GetMapping("/getAllArticles")
    public ResponseEntity<List<ArticleResp>> getAllArticles() throws IOException {
        List<ArticleResp> articles = articleService.getAllArticles();
        return ResponseEntity.ok(articles);
    }

    @GetMapping("/getPendingArticles")
    public ResponseEntity<List<ArticleResp>> getPendingArticles() throws IOException {
        List<ArticleResp> articles = articleService.getPendingArticles();
        return ResponseEntity.ok(articles);
    }

    @GetMapping("/getDataInfoByArticleId")
    public ResponseEntity<List<ArticleResp>> getArticlesByArticleId(@RequestParam Long articleId) throws IOException {
        List<ArticleResp> articles = articleService.getArticlesByArticleId(articleId);
        return ResponseEntity.ok(articles);
    }


    @PostMapping("/updateStatus")
    public ResponseEntity<Map<String, Object>> updateArticleStatus(
            @RequestParam Long articleId,
            @RequestParam String status
        ) {
        System.out.println("word:"+articleId+status);
        Map<String, Object> response = new HashMap<>();
        try {
            articleService.updateArticleStatus(articleId, status);
            response.put("success", true);
            response.put("message", "文章状态更新成功");
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            response.put("success", false);
            response.put("message", "更新文章状态失败: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "服务器错误: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }

    @GetMapping("/getApprovedArticles")
    public ResponseEntity<List<ArticleResp>> getApprovedArticles() throws IOException {
        List<ArticleResp> articles = articleService.getApprovedArticles();
        return ResponseEntity.ok(articles);
    }

    @GetMapping("/getRejectedArticles")
    public ResponseEntity<List<ArticleResp>> getRejectedArticles() throws IOException {
        List<ArticleResp> articles = articleService.getRejectedArticles();
        return ResponseEntity.ok(articles);
    }
    @PostMapping("/updatePublicStatus")
    public ResponseEntity<Map<String, Object>> updatePublicStatus(
            @RequestParam Long articleId,
            @RequestParam Long userId,
            @RequestParam Boolean isPublicInFollow
    ) {
        System.out.println("article:"+articleId);
        Map<String, Object> response = new HashMap<>();
        try {
            articleService.updateArticlePublicStatus(articleId, userId, isPublicInFollow);
            response.put("success", true);
            response.put("message", "文章公開狀態更新成功");
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            response.put("success", false);
            response.put("message", "更新公開狀態失敗: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "服務器錯誤: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }
}