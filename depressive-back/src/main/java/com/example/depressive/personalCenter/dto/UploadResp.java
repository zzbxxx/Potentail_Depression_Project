package com.example.depressive.personalCenter.dto;

import lombok.Data;

@Data
public class UploadResp {
    private boolean success;
    private String url;
    private String publicUrl; // 添加 publicUrl 字段

    public UploadResp(boolean success, String url) {
        this.success = success;
        this.url = url;
    }

    public UploadResp(boolean success, String url, String publicUrl) {
        this.success = success;
        this.url = url;
        this.publicUrl = publicUrl;
    }
}