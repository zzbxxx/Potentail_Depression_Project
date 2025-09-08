package com.example.depressive.login.dto;

import jakarta.validation.constraints.NotBlank;

public class GuestLoginRequest {
    @NotBlank
    private String newDeviceFingerprint;

    @NotBlank
    private String deviceInfo;

    public String getDeviceInfo() { return deviceInfo; }

    public void setDeviceInfo(String deviceInfo) { this.deviceInfo = deviceInfo; }

    public String getNewDeviceFingerprint() { return newDeviceFingerprint; }
    public void setNewDeviceFingerprint(String newDeviceFingerprint) { this.newDeviceFingerprint = newDeviceFingerprint; }
}