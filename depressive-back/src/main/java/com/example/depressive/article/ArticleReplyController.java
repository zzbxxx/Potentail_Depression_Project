package com.example.depressive.article;
import com.example.depressive.article.dto.ReplyResp;
import com.example.depressive.article.entity.Reply;
import com.example.depressive.article.repository.ReplyRepository;
import com.example.depressive.login.entity.User;
import com.example.depressive.login.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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

    // 创建评论
    @PostMapping("/putReply")
    public ResponseEntity<Map<String, Object>> createComment(
            @RequestParam String userId,
            @RequestBody Reply Reply) {
        Map<String, Object> response = new HashMap<>();
        try {
            Long id = Long.parseLong(userId);
            User user = userRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("用户不存在"));

            // 设置评论的 userId 和其他必要字段
            Reply.setUserId(id);
            Reply.setCreatedAt(LocalDateTime.now());
            Reply.setUpdatedAt(LocalDateTime.now());
            Reply.setLikeCount(0);
            Reply.setIsDeleted(false);

            replyRepository.save(Reply);
            response.put("success", true);
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

    @GetMapping("/getArticleReply/{articleId}")
    public ResponseEntity<Map<String, Object>> getReplysByArticle(@PathVariable Long articleId) {
        Map<String, Object> response = new HashMap<>();
        try {
            List<Reply> comments = replyRepository.findByArticleIdAndIsDeletedFalse(articleId);
            List<ReplyResp> commentResps = comments.stream().map(comment -> {
                ReplyResp resp = new ReplyResp();
                resp.setId(comment.getId());
                resp.setArticleId(comment.getArticleId());
                resp.setUserId(comment.getUserId());
                resp.setParentId(comment.getParentId());
                resp.setContent(comment.getContent());
                resp.setCreatedAt(comment.getCreatedAt());
                resp.setLikeCount(comment.getLikeCount());

                // 获取用户信息
                userRepository.findById(comment.getUserId()).ifPresent(user -> {
                    resp.setNickname(user.getNickname());
                    resp.setAvatar(user.getAvatar());
                    resp.setEmail(user.getEmail());
                });

                return resp;
            }).collect(Collectors.toList());

            response.put("success", true);
            response.put("data", commentResps);
            response.put("message", "评论查询成功");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "评论查询失败: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }
}