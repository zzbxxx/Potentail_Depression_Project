package com.example.depressive.personalCenter.service;

import com.example.depressive.personalCenter.dto.UploadResp;
import com.example.depressive.util.S3Util;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedPutObjectRequest;

import java.time.Duration;
import java.time.Instant;

@Service
public class AvatarService {

    @Autowired
    private S3Client s3Client;

    @Autowired
    private S3Presigner s3Presigner;

    @Autowired
    private S3Util s3Util;

    public UploadResp getPresignedUrl(String fileName) {
        String key = "avatars/" + Instant.now().toEpochMilli() + "-" + fileName;
        String contentType = s3Util.getContentTypeFromFileName(fileName);
        System.out.println("FileName: " + fileName + ", Key: " + key + ", Content-Type: " + contentType);

        PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                .bucket(s3Util.getBucketName())
                .key(key)
                .contentType(contentType)
                .build();

        PresignedPutObjectRequest presignedPutRequest = s3Presigner.presignPutObject(r ->
                r.signatureDuration(Duration.ofMinutes(10))
                        .putObjectRequest(putObjectRequest));

        String accessUrl = s3Util.generatePublicUrl(key);
        if (accessUrl == null) {
            GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                    .bucket(s3Util.getBucketName())
                    .key(key)
                    .build();
            PresignedGetObjectRequest presignedGetRequest = s3Presigner.presignGetObject(r ->
                    r.signatureDuration(Duration.ofHours(1))
                            .getObjectRequest(getObjectRequest));
            accessUrl = presignedGetRequest.url().toString();
        }

        System.out.println("Presigned Upload URL: " + presignedPutRequest.url().toString());
        System.out.println("Access URL: " + accessUrl);

        return new UploadResp(true, presignedPutRequest.url().toString(), accessUrl);
    }

    public UploadResp getPresignedDownloadUrl(String key) {
        if ("allPublic".equals(s3Util.getBucketType())) {
            return new UploadResp(false, null, "公有桶無需預簽名下載");
        }

        GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                .bucket(s3Util.getBucketName())
                .key(key)
                .build();

        PresignedGetObjectRequest presignedRequest = s3Presigner.presignGetObject(r ->
                r.signatureDuration(Duration.ofHours(1))
                        .getObjectRequest(getObjectRequest));

        return new UploadResp(true, presignedRequest.url().toString(), null);
    }
}
