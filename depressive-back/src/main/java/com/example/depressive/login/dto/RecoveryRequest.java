package com.example.depressive.login.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class RecoveryRequest {
    @NotBlank
    private String code;

    @NotBlank
    private String newDeviceFingerprint;
}