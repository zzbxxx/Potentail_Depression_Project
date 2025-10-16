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


    public List<ArticleResp> getArticlesByUserId(Long userId) throws IOException {
        List<Article> articles = articleRepository.findByUserIdAndStatus(
                userId, Article.ArticleStatus.APPROVED
        );
        return articles.stream().map(article -> {
            ArticleResp resp = new ArticleResp();
            resp.setId(article.getId());
            resp.setTitle(article.getTitle());
            resp.setArticleType(article.getArticleType().name());
            resp.setTopics(article.getTopics().stream()
                    .map(topic -> topic.getName())
                    .collect(Collectors.toSet()));
            try {
                resp.setBlocks(objectMapper.readValue(
                        article.getContent(),
                        new com.fasterxml.jackson.core.type.TypeReference<List<ArticleResp.BlockDTO>>() {}
                ));
            } catch (IOException e) {
                throw new RuntimeException("解析文章內容失敗", e);
            }
            resp.setStatus(article.getStatus().name());
            resp.setCreatedAt(article.getCreatedAt());
            resp.setNickname(article.getUser().getNickname());
            resp.setAvatar(article.getUser().getAvatar());
            resp.setUserId(article.getUser().getId());
            resp.setIsPublicInFollow(article.getIsPublicInFollow()); // 已修復
            return resp;
        }).collect(Collectors.toList());
    }

    public void updateArticlePublicStatus(Long articleId, Long userId, Boolean isPublicInFollow) {
        Article article = articleRepository.findById(articleId)
                .orElseThrow(() -> new IllegalArgumentException("文章不存在"));

        // 驗證操作者是否為文章作者
        if (!article.getUser().getId().equals(userId)) {
            throw new IllegalArgumentException("僅限文章作者修改公開狀態");
        }

        article.setIsPublicInFollow(isPublicInFollow);
        article.setUpdatedAt(LocalDateTime.now());
        articleRepository.save(article);
    }
    @Transactional
    public Article createArticle(ArticleReq articleDTO, MultipartFile[] images) throws IOException {
        // 驗證 userId
        User user = userRepository.findById(articleDTO.getUserId())
                .orElseThrow(() -> new IllegalArgumentException("無效的用戶 ID: " + articleDTO.getUserId()));

        // 驗證話題數量
        if (articleDTO.getTopics().size() > 3) {
            throw new IllegalArgumentException("最多只能選擇 3 個話題");
        }

        // 處理話題
        Set<Topic> topics = new HashSet<>();
        for (String topicName : articleDTO.getTopics()) {
            Topic topic = topicRepository.findByNameAndCategoryIn(topicName, List.of(Topic.Category.article, Topic.Category.both))
                    .orElseGet(() -> {
                        Topic newTopic = new Topic();
                        newTopic.setName(topicName);
                        newTopic.setCreatedAt(LocalDateTime.now());
                        newTopic.setCategory(Topic.Category.article); // 設置為 article
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

        // 保存到資料庫
        Article savedArticle = articleRepository.save(article);

        // 快取到 Redis
        try {
            String json = objectMapper.writeValueAsString(savedArticle);
            redisTemplate.opsForValue().set("article:" + savedArticle.getId(), json, 3600, TimeUnit.SECONDS);
            for (Topic topic : topics) {
                redisTemplate.opsForZSet().add("topics:" + topic.getId() + ":articles", String.valueOf(savedArticle.getId()), System.currentTimeMillis());
            }
        } catch (Exception e) {
            System.err.println("快取文章失敗: " + e.getMessage());
        }

        return savedArticle;
    }

    @Transactional
    public Article updateArticle(ArticleReq articleDTO, MultipartFile[] images) throws IOException {
        // ✅ 查找现有文章
        Article article = articleRepository.findById(articleDTO.getId())
                .orElseThrow(() -> new IllegalArgumentException("文章不存在: " + articleDTO.getId()));

        // ✅ 验证用户权限
        User user = userRepository.findById(articleDTO.getUserId())
                .orElseThrow(() -> new IllegalArgumentException("用户不存在"));
        if (!article.getUser().getId().equals(user.getId())) {
            throw new IllegalArgumentException("无权限修改他人文章");
        }

        // ✅ 验证话题数量
        if (articleDTO.getTopics().size() > 3) {
            throw new IllegalArgumentException("最多只能选择 3 个话题");
        }

        // 处理话题（同 createArticle）
        Set<Topic> topics = new HashSet<>();
        for (String topicName : articleDTO.getTopics()) {
            Topic topic = topicRepository.findByNameAndCategoryIn(topicName, List.of(Topic.Category.article, Topic.Category.both))
                    .orElseGet(() -> {
                        Topic newTopic = new Topic();
                        newTopic.setName(topicName);
                        newTopic.setCreatedAt(LocalDateTime.now());
                        newTopic.setCategory(Topic.Category.article);
                        return topicRepository.save(newTopic);
                    });
            topics.add(topic);
        }

        // 处理块内容（同 createArticle）
        List<Map<String, String>> processedBlocks = new ArrayList<>();
        int imageIndex = 0;
        for (ArticleReq.BlockDTO block : articleDTO.getBlocks()) {
            Map<String, String> blockMap = new HashMap<>();
            blockMap.put("type", block.getType());
            if ("text".equals(block.getType())) {
                String sanitizedContent = htmlSanitizer.sanitize(block.getContent());
                blockMap.put("content", sanitizedContent);
            } else if ("image".equals(block.getType())) {
                String imageUrl;
                if (imageIndex < images.length && images[imageIndex] != null) {
                    // 新上传图片
                    MultipartFile image = images[imageIndex++];
                    if (!image.getContentType().startsWith("image/") || image.getSize() > 2 * 1024 * 1024) {
                        throw new IllegalArgumentException("图片格式无效或大小超过 2MB");
                    }
                    String fileName = "articles/" + Instant.now().toEpochMilli() + "-" + image.getOriginalFilename();
                    String contentType = s3Util.getContentTypeFromFileName(image.getOriginalFilename());
                    PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                            .bucket(s3Util.getBucketName())
                            .key(fileName)
                            .contentType(contentType)
                            .build();
                    s3Client.putObject(putObjectRequest, software.amazon.awssdk.core.sync.RequestBody.fromBytes(image.getBytes()));
                    imageUrl = s3Util.generatePublicUrl(fileName);
                } else {
                    // 保留原图片
                    imageUrl = block.getContent();
                    imageIndex++;
                }
                blockMap.put("content", imageUrl);
            } else if ("link".equals(block.getType())) {
                blockMap.put("content", block.getContent());
            }
            processedBlocks.add(blockMap);
        }

        // ✅ 更新文章
        article.setTitle(articleDTO.getTitle());
        article.setArticleType(ArticleType.valueOf(articleDTO.getArticleType().toUpperCase()));
        article.setContentFromJson(processedBlocks);
        article.setTopics(topics);
        article.setUpdatedAt(LocalDateTime.now());

        // 保存
        Article updatedArticle = articleRepository.save(article);

        // 更新 Redis 缓存
        try {
            String json = objectMapper.writeValueAsString(updatedArticle);
            redisTemplate.opsForValue().set("article:" + updatedArticle.getId(), json, 3600, TimeUnit.SECONDS);
        } catch (Exception e) {
            System.err.println("缓存文章失败: " + e.getMessage());
        }

        return updatedArticle;
    }

    private String generatePresignedUrl(String key) {
        software.amazon.awssdk.services.s3.model.GetObjectRequest getObjectRequest = software.amazon.awssdk.services.s3.model.GetObjectRequest.builder()
                .bucket(s3Util.getBucketName())
                .key(key)
                .build();
        return s3Presigner.presignGetObject(r -> r.signatureDuration(Duration.ofHours(1)).getObjectRequest(getObjectRequest)).url().toString();
    }

    // 获取所有文章（保持不变）
    @Transactional(readOnly = true)
    public List<ArticleResp> getAllArticles() throws IOException {
        List<Article> articles = articleRepository.findAll();
        return articles.stream().map(this::convertToResp).collect(Collectors.toList());
    }

    // 获取待审核文章
    @Transactional(readOnly = true)
    public List<ArticleResp> getPendingArticles() throws IOException {
        List<Article> articles = articleRepository.findByStatus(Article.ArticleStatus.DRAFT);
        return articles.stream().map(this::convertToResp).collect(Collectors.toList());
    }

    // 获取单篇文章
    @Transactional(readOnly = true)
    public List<ArticleResp> getArticlesByArticleId(Long articleId) throws IOException {
        if (articleId == null || articleId <= 0) {
            throw new IllegalArgumentException("无效的文章 ID: " + articleId);
        }

        String cacheKey = "article:" + articleId;
        Article article;
        try {
            String cachedJson = redisTemplate.opsForValue().get(cacheKey);
            if (cachedJson != null) {
                article = objectMapper.readValue(cachedJson, Article.class);
            } else {
                article = articleRepository.findById(articleId)
                        .orElseThrow(() -> new IllegalArgumentException("文章不存在: " + articleId));
                String json = objectMapper.writeValueAsString(article);
                redisTemplate.opsForValue().set(cacheKey, json, 3600, TimeUnit.SECONDS);
            }
        } catch (Exception e) {
            System.err.println("从 Redis 读取文章失败: " + e.getMessage());
            article = articleRepository.findById(articleId)
                    .orElseThrow(() -> new IllegalArgumentException("文章不存在: " + articleId));
        }

        return Collections.singletonList(convertToResp(article));
    }

    // 更新文章状态
    @Transactional
    public void updateArticleStatus(Long articleId, String status) {
        if (articleId == null || articleId <= 0) {
            throw new IllegalArgumentException("无效的文章 ID: " + articleId);
        }
//        if (adminId == null || adminId <= 0) {
//            throw new IllegalArgumentException("无效的管理员 ID: " + adminId);
//        }

        // 验证管理员
//        userRepository.findById(adminId)
//                .orElseThrow(() -> new IllegalArgumentException("管理员不存在: " + adminId));

        Article article = articleRepository.findById(articleId)
                .orElseThrow(() -> new IllegalArgumentException("文章不存在: " + articleId));

        try {
            Article.ArticleStatus articleStatus = Article.ArticleStatus.valueOf(status.toUpperCase());
            article.setStatus(articleStatus);
            article.setUpdatedAt(LocalDateTime.now());
            articleRepository.save(article);

            // 更新 Redis 缓存
            try {
                String json = objectMapper.writeValueAsString(article);
                redisTemplate.opsForValue().set("article:" + articleId, json, 3600, TimeUnit.SECONDS);
            } catch (Exception e) {
                System.err.println("缓存文章失败: " + e.getMessage());
            }
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("无效的状态: " + status);
        }
    }

    // 转换为 ArticleResp DTO
    private ArticleResp convertToResp(Article article) {
        try {
            ArticleResp dto = new ArticleResp();
            dto.setId(article.getId());
            dto.setTitle(article.getTitle());
            dto.setArticleType(article.getArticleType().name().toLowerCase());
            dto.setTopics(article.getTopics().stream()
                    .map(Topic::getName)
                    .collect(Collectors.toSet()));
            dto.setBlocks(article.getContentAsJson().stream()
                    .map(block -> {
                        ArticleResp.BlockDTO blockDTO = new ArticleResp.BlockDTO();
                        blockDTO.setType(block.get("type"));
                        blockDTO.setContent(block.get("content"));
                        return blockDTO;
                    })
                    .collect(Collectors.toList()));
            dto.setStatus(article.getStatus().name());
            dto.setCreatedAt(article.getCreatedAt());
            User user = article.getUser();
            if (user != null) {
                dto.setNickname(user.getNickname());
                dto.setAvatar(user.getAvatar());
                dto.setUserId(user.getId());
            }
            return dto;
        } catch (IOException e) {
            throw new RuntimeException("解析文章内容失败: " + article.getId(), e);
        }
    }

    @Transactional(readOnly = true)
    public List<ArticleResp> getApprovedArticles() throws IOException {
        // 修改為查詢 status=APPROVED 且 isPublicInFollow=true 的文章
        List<Article> articles = articleRepository.findByStatusAndIsPublicInFollow(
                Article.ArticleStatus.APPROVED, true
        );
        return articles.stream().map(this::convertToResp).collect(Collectors.toList());
    }


    public List<ArticleResp> getRejectedArticles() {
        List<Article> articles = articleRepository.findByStatus(Article.ArticleStatus.REJECTED);
        return articles.stream().map(this::convertToResp).collect(Collectors.toList());
    }
}