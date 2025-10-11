package com.example.depressive.notification.entity;

import com.example.depressive.login.entity.User;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.Data;

import java.util.Date;

@Entity
@Table(name = "notification")
@Data
public class Notification {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    @JsonManagedReference // 标记为管理端，允许序列化
    private User user; // 通知接收者

    @Column(nullable = false)
    private String title; // 通知标题

    @Column(columnDefinition = "TEXT")
    private String content; // 通知内容

    @Column(nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date timestamp; // 创建时间

    @Column(name = "`read`")
    private boolean read = false; // 是否已读

    @Column(name = "notification_type", nullable = false)
    private String notificationType = "GENERAL";

    @Column(name = "trigger_user_id") // 新增：觸發者ID
    private Long triggerUserId;
}