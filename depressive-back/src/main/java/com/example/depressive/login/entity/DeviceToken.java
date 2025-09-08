package com.example.depressive.login.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "device_tokens")
public class DeviceToken {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "token_value", nullable = false)
    private String tokenValue;

    @Column(name = "fingerprint_hash", nullable = false)
    private String fingerprintHash;

    @Column(nullable = false)
    private LocalDateTime expiresAt;

    @Column(updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "device_info")
    private String deviceInfo;

}
