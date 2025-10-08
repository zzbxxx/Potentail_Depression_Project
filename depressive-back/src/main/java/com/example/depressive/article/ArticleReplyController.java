package com.example.depressive.article;

import com.example.depressive.article.dto.ArticleResp;
import com.example.depressive.article.dto.ReplyResp;
import com.example.depressive.article.entity.Reply;
import com.example.depressive.article.repository.ReplyRepository;
import com.example.depressive.article.entity.Article;
import com.example.depressive.article.repository.ArticleRepository;
import com.example.depressive.article.service.ArticleService;
import com.example.depressive.login.entity.User;
import com.example.depressive.login.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/reply")
@CrossOrigin(origins = "http://localhost:5173")
public class ArticleReplyController {

    @Autowired
    private ReplyRepository replyRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ArticleRepository articleRepository;


    // 创建评论
    @PostMapping("/putReply")
    public ResponseEntity<Map<String, Object>> createComment(
            @RequestParam String userId,
            @RequestBody Reply reply) {
        Map<String, Object> response = new HashMap<>();
        try {
            Long id = Long.parseLong(userId);
            User user = userRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("用户不存在"));

            // 验证文章是否存在
            Article article = articleRepository.findById(reply.getArticleId())
                    .orElseThrow(() -> new RuntimeException("文章不存在"));

            // 设置评论的 userId 和其他必要字段
            reply.setUserId(id);
            reply.setCreatedAt(LocalDateTime.now());
            reply.setUpdatedAt(LocalDateTime.now());
            reply.setLikeCount(0);
            reply.setIsDeleted(false);

            replyRepository.save(reply);

            // 返回创建的评论信息
            ReplyResp resp = new ReplyResp();
            resp.setId(reply.getId());
            resp.setArticleId(reply.getArticleId());
            resp.setUserId(reply.getUserId());
            resp.setParentId(reply.getParentId());
            resp.setContent(reply.getContent());
            resp.setCreatedAt(reply.getCreatedAt());
            resp.setLikeCount(reply.getLikeCount());
            resp.setNickname(user.getNickname());
            resp.setAvatar(user.getAvatar());
            resp.setEmail(user.getEmail());

            response.put("success", true);
            response.put("data", resp);
            response.put("message", "评论创建成功");
            return ResponseEntity.ok(response);
        } catch (NumberFormatException e) {
            response.put("success", false);
            response.put("message", "无效的用户ID");
            return ResponseEntity.status(400).body(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "评论创建失败: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }

    // 查询文章的评论
    @GetMapping("/getArticleReply")
    public ResponseEntity<Map<String, Object>> getReplysByArticle(
            @RequestParam long articleId,
            @RequestParam(required = false) Long userId) {
        Map<String, Object> response = new HashMap<>();
        try {
            // 未登录用户无法查看任何评论
            if (userId == null) {
                response.put("success", false);
                response.put("message", "用户未登录，无法查看私密留言");
                return ResponseEntity.status(401).body(response);
            }

            // 验证文章是否存在
            Article article = articleRepository.findById(articleId)
                    .orElseThrow(() -> new RuntimeException("文章不存在"));
            Long articleAuthorId = article.getUser().getId();

            // 查询评论，仅返回用户自己发布的或作者有权限查看的评论
            List<Reply> replies = replyRepository.findByArticleIdAndIsDeletedFalse(articleId);
            List<ReplyResp> replyResp = replies.stream()
                    .filter(reply -> reply.getUserId().equals(userId) || userId.equals(articleAuthorId))
                    .map(reply -> {
                        ReplyResp resp = new ReplyResp();
                        resp.setId(reply.getId());
                        resp.setArticleId(reply.getArticleId());
                        resp.setUserId(reply.getUserId());
                        resp.setParentId(reply.getParentId());
                        resp.setContent(reply.getContent());
                        resp.setCreatedAt(reply.getCreatedAt());
                        resp.setLikeCount(reply.getLikeCount());

                        // 获取用户信息（仅在必要时查询）
                        userRepository.findById(reply.getUserId()).ifPresent(user -> {
                            resp.setNickname(user.getNickname());
                            resp.setAvatar(user.getAvatar());
                            resp.setEmail(user.getEmail());
                        });

                        return resp;
                    })
                    .collect(Collectors.toList());

            response.put("success", true);
            response.put("data", replyResp);
            response.put("message", "评论查询成功");
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            response.put("success", false);
            response.put("message", "评论查询失败: " + e.getMessage());
            return ResponseEntity.status(400).body(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "服务器错误: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }

    // 删除评论
    @DeleteMapping("/deleteReply")
    public ResponseEntity<Map<String, Object>> deleteComment(
            @RequestParam Long replyId,
            @RequestParam Long userId) {
        Map<String, Object> response = new HashMap<>();
        try {
            Reply reply = replyRepository.findById(replyId)
                    .orElseThrow(() -> new RuntimeException("评论不存在"));

            // 获取文章的作者 ID
            Article article = articleRepository.findById(reply.getArticleId())
                    .orElseThrow(() -> new RuntimeException("文章不存在"));
            Long articleAuthorId = article.getUser().getId();

            // 验证用户是否有权限删除（评论发布者或文章作者）
            if (!reply.getUserId().equals(userId) && !userId.equals(articleAuthorId)) {
                response.put("success", false);
                response.put("message", "无权限删除此评论");
                return ResponseEntity.status(403).body(response);
            }

            // 软删除：设置 isDeleted 为 true
            reply.setIsDeleted(true);
            reply.setUpdatedAt(LocalDateTime.now());
            replyRepository.save(reply);

            response.put("success", true);
            response.put("message", "评论删除成功");
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.status(400).body(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "评论删除失败: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }
}