package com.example.depressive.notification.dto;

import lombok.Data;

import java.util.Date;

@Data
public class NotificationDTO {
    private Long id;
    private Long userId;
    private String title;
    private String content;
    private Date timestamp;
    private boolean read;
    private String notificationType;
    private Long triggerUserId;
    private String triggerUsername;
}