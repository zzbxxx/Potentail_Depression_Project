package com.example.depressive.login.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class RecoveryCodeResponse {
    private String code;
    private LocalDateTime expiresAt;
}