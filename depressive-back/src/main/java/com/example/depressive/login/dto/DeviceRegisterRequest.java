package com.example.depressive.login.dto;


import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class DeviceRegisterRequest {
    @NotBlank
    private String fingerprint;

    @NotBlank
    private String deviceInfo;
}