package com.example.depressive.article.service;
import com.example.depressive.article.dto.ArticleReq;
import com.example.depressive.article.dto.ArticleResp;
import com.example.depressive.article.entity.Article;
import com.example.depressive.article.entity.ArticleType;
import com.example.depressive.article.entity.Topic;
import com.example.depressive.article.repository.ArticleRepository;
import com.example.depressive.article.repository.TopicRepository;
import com.example.depressive.login.entity.User;
import com.example.depressive.login.repository.UserRepository;
import com.example.depressive.util.S3Util;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.owasp.html.HtmlPolicyBuilder;
import org.owasp.html.PolicyFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.PresignedPutObjectRequest;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
public class ArticleService {

    @Autowired
    private ArticleRepository articleRepository;

    @Autowired
    private TopicRepository topicRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private S3Client s3Client;

    @Autowired
    private S3Presigner s3Presigner;

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private S3Util s3Util;

    private final PolicyFactory htmlSanitizer = new HtmlPolicyBuilder()
            .allowElements("p", "strong", "em", "a", "br")
            .allowAttributes("href").onElements("a")
            .toFactory();

    @Transactional
    public Article createArticle(ArticleReq articleDTO, MultipartFile[] images) throws IOException {
        // 驗證 userId
        System.out.println("article:" + articleDTO);
        User user = userRepository.findById(articleDTO.getUserId())
                .orElseThrow(() -> new IllegalArgumentException("無效的用戶 ID: " + articleDTO.getUserId()));

        // 驗證話題數量
        if (articleDTO.getTopics().size() > 3) {
            throw new IllegalArgumentException("最多只能選擇3個話題");
        }

        // 處理話題
        Set<Topic> topics = new HashSet<>();
        for (String topicName : articleDTO.getTopics()) {
            Topic topic = topicRepository.findByName(topicName)
                    .orElseGet(() -> {
                        Topic newTopic = new Topic();
                        newTopic.setName(topicName);
                        newTopic.setCreatedAt(LocalDateTime.now());
                        return topicRepository.save(newTopic);
                    });
            topics.add(topic);
        }

        // 處理塊內容並替換圖片 blob URL
        List<Map<String, String>> processedBlocks = new ArrayList<>();
        int imageIndex = 0;
        for (ArticleReq.BlockDTO block : articleDTO.getBlocks()) {
            Map<String, String> blockMap = new HashMap<>();
            blockMap.put("type", block.getType());
            if ("text".equals(block.getType())) {
                String sanitizedContent = htmlSanitizer.sanitize(block.getContent());
                blockMap.put("content", sanitizedContent);
            } else if ("image".equals(block.getType())) {
                if (imageIndex >= images.length) {
                    throw new IllegalArgumentException("圖片文件數量不足");
                }
                MultipartFile image = images[imageIndex++];
                if (!image.getContentType().startsWith("image/") || image.getSize() > 2 * 1024 * 1024) {
                    throw new IllegalArgumentException("圖片格式無效或大小超過 2MB");
                }
                String fileName = "articles/" + Instant.now().toEpochMilli() + "-" + image.getOriginalFilename();
                String contentType = s3Util.getContentTypeFromFileName(image.getOriginalFilename());
                PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                        .bucket(s3Util.getBucketName())
                        .key(fileName)
                        .contentType(contentType)
                        .build();
                s3Client.putObject(putObjectRequest, software.amazon.awssdk.core.sync.RequestBody.fromBytes(image.getBytes()));
                String imageUrl = s3Util.generatePublicUrl(fileName);
                if (imageUrl == null) {
                    imageUrl = generatePresignedUrl(fileName);
                }
                blockMap.put("content", imageUrl);
            } else if ("link".equals(block.getType())) {
                blockMap.put("content", block.getContent());
            }
            processedBlocks.add(blockMap);
        }

        // 創建 Article 實體
        Article article = new Article();
        article.setUser(user);
        article.setTitle(articleDTO.getTitle());
        try {
            article.setArticleType(ArticleType.valueOf(articleDTO.getArticleType().toUpperCase()));
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("無效的文章類型: " + articleDTO.getArticleType());
        }
        article.setContentFromJson(processedBlocks);
        article.setTopics(topics);
        article.setStatus(Article.ArticleStatus.DRAFT);

        // 保存到數據庫
        Article savedArticle = articleRepository.save(article);

        // 緩存到 Redis
        try {
            String json = objectMapper.writeValueAsString(savedArticle);
            redisTemplate.opsForValue().set("article:" + savedArticle.getId(), json, 3600, TimeUnit.SECONDS);
            for (Topic topic : topics) {
                redisTemplate.opsForZSet().add("topics:" + topic.getId() + ":articles", String.valueOf(savedArticle.getId()), System.currentTimeMillis());
            }
        } catch (Exception e) {
            System.err.println("緩存文章失敗: " + e.getMessage());
        }

        return savedArticle;
    }

    private String generatePresignedUrl(String key) {
        software.amazon.awssdk.services.s3.model.GetObjectRequest getObjectRequest = software.amazon.awssdk.services.s3.model.GetObjectRequest.builder()
                .bucket(s3Util.getBucketName())
                .key(key)
                .build();
        return s3Presigner.presignGetObject(r -> r.signatureDuration(Duration.ofHours(1)).getObjectRequest(getObjectRequest)).url().toString();
    }

    @Transactional(readOnly = true)
    public List<ArticleResp> getAllArticles() throws IOException {
        // 從數據庫獲取所有文章
        List<Article> articles = articleRepository.findAll();

        // 轉換為 DTO 列表
        return articles.stream().map(article -> {
            // 嘗試從 Redis 獲取緩存
            String cacheKey = "article:" + article.getId();
            Article cachedArticle = null;
            try {
                String cachedJson = redisTemplate.opsForValue().get(cacheKey);
                if (cachedJson != null) {
                    cachedArticle = objectMapper.readValue(cachedJson, Article.class);
                }
            } catch (Exception e) {
                System.err.println("從 Redis 讀取文章失敗: " + e.getMessage());
            }

            // 如果無緩存，使用數據庫數據
            Article targetArticle = cachedArticle != null ? cachedArticle : article;

            // 獲取用戶信息
            User user = targetArticle.getUser();
            if (user == null) {
                throw new IllegalArgumentException("文章無關聯用戶: " + targetArticle.getId());
            }

            // 轉換為 DTO
            ArticleResp dto = new ArticleResp();
            dto.setId(targetArticle.getId());
            dto.setTitle(targetArticle.getTitle());
            dto.setArticleType(targetArticle.getArticleType().name().toLowerCase());
            dto.setTopics(targetArticle.getTopics().stream()
                    .map(Topic::getName)
                    .collect(Collectors.toSet()));
            try {
                dto.setBlocks(targetArticle.getContentAsJson().stream()
                        .map(block -> {
                            ArticleResp.BlockDTO blockDTO = new ArticleResp.BlockDTO();
                            blockDTO.setType(block.get("type"));
                            blockDTO.setContent(block.get("content"));
                            return blockDTO;
                        })
                        .collect(Collectors.toList()));
            } catch (IOException e) {
                throw new RuntimeException("解析文章內容失敗: " + targetArticle.getId(), e);
            }
            dto.setStatus(targetArticle.getStatus().name());
            dto.setCreatedAt(targetArticle.getCreatedAt());
            dto.setNickname(user.getNickname());
            dto.setAvatar(user.getAvatar());
            dto.setUserId(user.getId());

            // 如果 Redis 無緩存，更新緩存
            if (cachedArticle == null) {
                try {
                    String json = objectMapper.writeValueAsString(targetArticle);
                    redisTemplate.opsForValue().set(cacheKey, json, 3600, TimeUnit.SECONDS);
                } catch (Exception e) {
                    System.err.println("緩存文章失敗: " + e.getMessage());
                }
            }

            return dto;
        }).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ArticleResp> getArticlesByArticleId(Long articleId) throws IOException {
        // 验证 articleId 是否有效
        if (articleId == null || articleId <= 0) {
            throw new IllegalArgumentException("无效的文章 ID: " + articleId);
        }

        // 尝试从 Redis 获取缓存
        String cacheKey = "article:" + articleId;
        Article cachedArticle = null;
        try {
            String cachedJson = redisTemplate.opsForValue().get(cacheKey);
            if (cachedJson != null) {
                cachedArticle = objectMapper.readValue(cachedJson, Article.class);
            }
        } catch (Exception e) {
            System.err.println("从 Redis 读取文章失败: " + e.getMessage());
        }

        // 如果缓存中没有，查询数据库
        Article article;
        if (cachedArticle != null) {
            article = cachedArticle;
        } else {
            article = articleRepository.findById(articleId)
                    .orElseThrow(() -> new IllegalArgumentException("文章不存在: " + articleId));
            // 缓存到 Redis
            try {
                String json = objectMapper.writeValueAsString(article);
                redisTemplate.opsForValue().set(cacheKey, json, 3600, TimeUnit.SECONDS);
            } catch (Exception e) {
                System.err.println("缓存文章失败: " + e.getMessage());
            }
        }

        // 验证用户信息
        User user = article.getUser();
        if (user == null) {
            throw new IllegalArgumentException("文章无关联用户: " + article.getId());
        }

        // 转换为 ArticleResp DTO
        ArticleResp dto = new ArticleResp();
        dto.setId(article.getId());
        dto.setTitle(article.getTitle());
        dto.setArticleType(article.getArticleType().name().toLowerCase());
        dto.setTopics(article.getTopics().stream()
                .map(Topic::getName)
                .collect(Collectors.toSet()));
        try {
            dto.setBlocks(article.getContentAsJson().stream()
                    .map(block -> {
                        ArticleResp.BlockDTO blockDTO = new ArticleResp.BlockDTO();
                        blockDTO.setType(block.get("type"));
                        blockDTO.setContent(block.get("content"));
                        return blockDTO;
                    })
                    .collect(Collectors.toList()));
        } catch (IOException e) {
            throw new RuntimeException("解析文章内容失败: " + article.getId(), e);
        }
        dto.setStatus(article.getStatus().name());
        dto.setCreatedAt(article.getCreatedAt());
        dto.setNickname(user.getNickname());
        dto.setAvatar(user.getAvatar());
        dto.setUserId(user.getId());

        // 返回单篇文章的列表
        return Collections.singletonList(dto);
    }
}