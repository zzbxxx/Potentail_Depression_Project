package com.example.depressive.login.controller;

import com.example.depressive.login.dto.*;
import com.example.depressive.login.entity.DeviceToken;
import com.example.depressive.login.service.DeviceAuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/devices")
@RequiredArgsConstructor
public class DeviceController {
    private final DeviceAuthService authService;

    @PostMapping("/guest-login")
    public ResponseEntity<DeviceTokenResponse> guestLogin(@RequestBody @Valid GuestLoginRequest request) {
        DeviceToken token = authService.createGuestToken(request.getNewDeviceFingerprint());
        boolean usedFingerprint = token.getFingerprintHash() != null;
        return ResponseEntity.ok(new DeviceTokenResponse(
                token.getTokenValue(),
                token.getExpiresAt(),
                usedFingerprint
        ));
    }
    @PostMapping("/register")
    public ResponseEntity<RecoveryCodeResponse> registerDevice(
            @RequestBody @Valid DeviceRegisterRequest request) {

        System.out.println("request："+request);
        var code = authService.registerDevice(
                request.getFingerprint(),
                request.getDeviceInfo()
        );
        System.out.println("code："+code);
        return ResponseEntity.ok(new RecoveryCodeResponse(
                code.getCode(),
                code.getExpiresAt()
        ));
    }

    @PostMapping("/recover")
    public ResponseEntity<DeviceTokenResponse> recoverDevice(
            @RequestBody @Valid RecoveryRequest request) {

        var token = authService.verifyRecoveryCode(
                request.getCode(),
                request.getNewDeviceFingerprint()
        );

        boolean usedFingerprint = token.getFingerprintHash() != null;
        return ResponseEntity.ok(new DeviceTokenResponse(
                token.getTokenValue(),
                token.getExpiresAt(),
                usedFingerprint
        ));
    }
}