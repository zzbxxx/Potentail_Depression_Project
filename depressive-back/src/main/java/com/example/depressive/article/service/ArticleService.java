package com.example.depressive.article.service;

import com.example.depressive.article.dto.ArticleDTO;
import com.example.depressive.article.entity.Article;
import com.example.depressive.article.entity.Topic;
import com.example.depressive.article.repository.ArticleRepository;
import com.example.depressive.article.repository.TopicRepository;
import com.example.depressive.login.entity.User;
import com.example.depressive.login.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.owasp.html.HtmlPolicyBuilder;
import org.owasp.html.PolicyFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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

    @Value("${backblaze.b2.bucket-name}")
    private String bucketName;

    @Value("${backblaze.b2.bucket-type}")
    private String bucketType;

    @Value("${backblaze.b2.region}")
    private String region;

    private final PolicyFactory htmlSanitizer = new HtmlPolicyBuilder()
            .allowElements("p", "strong", "em", "a", "br")
            .allowAttributes("href").onElements("a")
            .toFactory();

    @Transactional
    public Article createArticle(ArticleDTO articleDTO, MultipartFile[] images) throws IOException {
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
        for (ArticleDTO.BlockDTO block : articleDTO.getBlocks()) {
            Map<String, String> blockMap = new HashMap<>();
            blockMap.put("type", block.getType());
            if ("text".equals(block.getType())) {
                // 過濾文本防止 XSS
                String sanitizedContent = htmlSanitizer.sanitize(block.getContent());
                blockMap.put("content", sanitizedContent);
            } else if ("image".equals(block.getType())) {
                // 驗證是否有對應的圖片文件
                if (imageIndex >= images.length) {
                    throw new IllegalArgumentException("圖片文件數量不足");
                }
                MultipartFile image = images[imageIndex++];
                if (!image.getContentType().startsWith("image/") || image.getSize() > 2 * 1024 * 1024) {
                    throw new IllegalArgumentException("圖片格式無效或大小超過 2MB");
                }
                // 上傳到 Backblaze B2
                String fileName = "articles/" + Instant.now().toEpochMilli() + "-" + image.getOriginalFilename();
                String contentType = getContentTypeFromFileName(image.getOriginalFilename());
                PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                        .bucket(bucketName)
                        .key(fileName)
                        .contentType(contentType)
                        .build();
                s3Client.putObject(putObjectRequest, software.amazon.awssdk.core.sync.RequestBody.fromBytes(image.getBytes()));
                String imageUrl = "allPublic".equals(bucketType)
                        ? String.format("https://f%s.backblazeb2.com/file/%s/%s", region.substring(region.length() - 3), bucketName, fileName)
                        : generatePresignedUrl(fileName);
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
            article.setArticleType(Article.ArticleType.valueOf(articleDTO.getArticleType().toUpperCase()));
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("無效的文章類型: " + articleDTO.getArticleType());
        }
        article.setContentFromJson(processedBlocks);
        article.setTopics(topics);
        article.setStatus(Article.ArticleStatus.DRAFT);

        // 保存到數據庫
        Article savedArticle = articleRepository.save(article);
//[{"type": "text", "content": "<p>《西西弗斯》</p>"}, {"type": "image", "content": "https://s3.us-east-005.backblazeb2.com/Potential-Depressive-Bucket/articles/1758198135573-FT.jpg?X-Amz-Algorithm=AWS4-HMAC-SHA256&X-Amz-Date=20250918T122217Z&X-Amz-SignedHeaders=host&X-Amz-Credential=00502081ecf03510000000002%2F20250918%2Fus-east-005%2Fs3%2Faws4_request&X-Amz-Expires=3600&X-Amz-Signature=4e2000bf1239f7adaf3e03adfd61086f9e08dfc3d6c8a8aa946fc8d62ef59b99"}, {"type": "image", "content": "https://s3.us-east-005.backblazeb2.com/Potential-Depressive-Bucket/articles/1758198137325-ReSleep.jpg?X-Amz-Algorithm=AWS4-HMAC-SHA256&X-Amz-Date=20250918T122217Z&X-Amz-SignedHeaders=host&X-Amz-Credential=00502081ecf03510000000002%2F20250918%2Fus-east-005%2Fs3%2Faws4_request&X-Amz-Expires=3600&X-Amz-Signature=0c7a5026c21b0244021f79db6e3b3780616abd1028b338e7d753ef1667a2d052"}]
        // 緩存到 Redis
        try {
            String json = objectMapper.writeValueAsString(savedArticle);
            redisTemplate.opsForValue().set("article:" + savedArticle.getId(), json, 3600, TimeUnit.SECONDS);
            for (Topic topic : topics) {
                redisTemplate.opsForZSet().add("topics:" + topic.getId() + ":articles", String.valueOf(savedArticle.getId()), System.currentTimeMillis());
            }
        } catch (Exception e) {
            // 日誌記錄，緩存失敗不影響保存
        }

        return savedArticle;
    }

    private String generatePresignedUrl(String key) {
        software.amazon.awssdk.services.s3.model.GetObjectRequest getObjectRequest = software.amazon.awssdk.services.s3.model.GetObjectRequest.builder()
                .bucket(bucketName)
                .key(key)
                .build();
        return s3Presigner.presignGetObject(r -> r.signatureDuration(Duration.ofHours(1)).getObjectRequest(getObjectRequest)).url().toString();
    }

    private String getContentTypeFromFileName(String fileName) {
        String extension = fileName.substring(fileName.lastIndexOf(".") + 1).toLowerCase();
        switch (extension) {
            case "jpg":
            case "jpeg":
                return "image/jpeg";
            case "png":
                return "image/png";
            case "gif":
                return "image/gif";
            default:
                return "application/octet-stream";
        }
    }
}