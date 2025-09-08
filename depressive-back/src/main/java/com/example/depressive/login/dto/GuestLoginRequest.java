package com.example.depressive.login.dto;

import jakarta.validation.constraints.NotBlank;

public class GuestLoginRequest {
    @NotBlank
    private String newDeviceFingerprint;

    public String getNewDeviceFingerprint() { return newDeviceFingerprint; }
    public void setNewDeviceFingerprint(String newDeviceFingerprint) { this.newDeviceFingerprint = newDeviceFingerprint; }
}