package com.example.depressive.notification.dto;

import lombok.Data;

import java.util.Date;

@Data
public class NotificationDTO {
    private Long id;
    private Long userId; // 只返回 userId 而不是整个 User 对象
    private String title;
    private String content;
    private Date timestamp;
    private boolean read;
    private String notificationType;
}