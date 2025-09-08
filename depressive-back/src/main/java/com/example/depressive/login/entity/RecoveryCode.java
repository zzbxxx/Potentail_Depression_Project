package com.example.depressive.login.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "recovery_codes")
public class RecoveryCode {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String codeHash;

    @Transient  // 不持久化到数据库
    @Getter
    private String code;

    @Column(nullable = false)
    private String fingerprintHash;

    private String deviceInfo;

    @Column(nullable = false)
    private LocalDateTime expiresAt;

    @Setter
    @Getter
    private boolean used = false;

    @Column(updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();


}