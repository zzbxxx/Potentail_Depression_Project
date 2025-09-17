package com.example.depressive.personalCenter.controller;

import com.example.depressive.personalCenter.dto.UploadResp;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.PresignedPutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;
import java.time.Duration;
import java.time.Instant;

@RestController
@RequestMapping("/api/picture")
@CrossOrigin(origins = "http://localhost:5173")
public class AvatarController {

    private final S3Client s3Client;
    private final S3Presigner s3Presigner;

    @Value("${backblaze.b2.bucket-name}")
    private String bucketName;

    @Value("${backblaze.b2.region}")
    private String region;

    // 假設一個變數表示桶類型（可從配置讀取或動態查詢）
    @Value("${backblaze.b2.bucket-type}")  // application.properties 中設定 allPrivate 或 allPublic
    private String bucketType;

    public AvatarController(S3Client s3Client, S3Presigner s3Presigner) {
        this.s3Client = s3Client;
        this.s3Presigner = s3Presigner;
    }

    @GetMapping("/get-presigned-url")
    public ResponseEntity<UploadResp> getPresignedUrl(@RequestParam String fileName) {
        String key = "avatars/" + Instant.now().toEpochMilli() + "-" + fileName;
        String contentType = getContentTypeFromFileName(fileName);
        System.out.println("FileName: " + fileName + ", Key: " + key + ", Content-Type: " + contentType);

        PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                .bucket(bucketName)
                .key(key)
                .contentType(contentType)
                .build();

        PresignedPutObjectRequest presignedPutRequest = s3Presigner.presignPutObject(r ->
                r.signatureDuration(Duration.ofMinutes(10))
                        .putObjectRequest(putObjectRequest));

        String accessUrl;
        if ("allPublic".equals(bucketType)) {
            // 公有桶：使用公開 URL
            accessUrl = String.format("https://f%s.backblazeb2.com/file/%s/%s",
                    region.substring(region.length() - 3), bucketName, key);
        } else {
            // 私有桶：生成預簽名下載 URL
            GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                    .bucket(bucketName)
                    .key(key)
                    .build();

            PresignedGetObjectRequest presignedGetRequest = s3Presigner.presignGetObject(r ->
                    r.signatureDuration(Duration.ofHours(1))  // 有效期 1 小時
                            .getObjectRequest(getObjectRequest));
            accessUrl = presignedGetRequest.url().toString();
        }

        System.out.println("Presigned Upload URL: " + presignedPutRequest.url().toString());
        System.out.println("Access URL: " + accessUrl);

        return ResponseEntity.ok(new UploadResp(true, presignedPutRequest.url().toString(), accessUrl));
    }

    // 私有桶額外接口：刷新預簽名下載 URL
    @GetMapping("/get-presigned-download-url")
    public ResponseEntity<UploadResp> getPresignedDownloadUrl(@RequestParam String key) {
        if ("allPublic".equals(bucketType)) {
            return ResponseEntity.badRequest().body(new UploadResp(false, null, "公有桶無需預簽名下載"));
        }

        GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                .bucket(bucketName)
                .key(key)
                .build();

        PresignedGetObjectRequest presignedRequest = s3Presigner.presignGetObject(r ->
                r.signatureDuration(Duration.ofHours(1))
                        .getObjectRequest(getObjectRequest));

        return ResponseEntity.ok(new UploadResp(true, presignedRequest.url().toString(), null));
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