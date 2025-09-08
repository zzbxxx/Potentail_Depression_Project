package com.example.depressive.login.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class DeviceTokenResponse {
    private String token;
    private LocalDateTime expiresAt;
    private boolean usedFingerprint;
    private Long userId;
}