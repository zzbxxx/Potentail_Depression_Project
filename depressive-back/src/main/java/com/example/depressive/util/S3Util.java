package com.example.depressive.util;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class S3Util {

    @Value("${backblaze.b2.bucket-name}")
    private String bucketName;

    @Value("${backblaze.b2.bucket-type}")
    private String bucketType;

    @Value("${backblaze.b2.region}")
    private String region;

    /**
     * 根據文件擴展名返回 MIME 類型
     */
    public String getContentTypeFromFileName(String fileName) {
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

    /**
     * 生成 Backblaze B2 公開 URL
     */
    public String generatePublicUrl(String key) {
        if ("allPublic".equals(bucketType)) {
            return String.format("https://f%s.backblazeb2.com/file/%s/%s",
                    region.substring(region.length() - 3), bucketName, key);
        }
        return null; // 非公開桶需使用預簽名 URL
    }

    /**
     * 獲取 bucketName
     */
    public String getBucketName() {
        return bucketName;
    }

    /**
     * 獲取 bucketType
     */
    public String getBucketType() {
        return bucketType;
    }
}