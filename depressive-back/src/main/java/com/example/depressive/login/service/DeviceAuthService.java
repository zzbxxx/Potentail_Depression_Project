package com.example.depressive.login.service;

import com.example.depressive.login.entity.DeviceToken;
import com.example.depressive.login.entity.RecoveryCode;
import com.example.depressive.login.exception.ExpiredCodeException;
import com.example.depressive.login.exception.InvalidCodeException;
import com.example.depressive.login.repository.DeviceRepository;
import com.example.depressive.login.repository.RecoveryCodeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.IntStream;

@Service
@Transactional
@RequiredArgsConstructor
public class DeviceAuthService {
    private final RecoveryCodeRepository codeRepo;
    private final DeviceRepository deviceRepo;
    private final EncryptionService encryptionService;

    public RecoveryCode registerDevice(String fingerprint, String deviceInfo) {
        RecoveryCode entity = new RecoveryCode();

        String plainCode = generateSecureCode();
        entity.setCode(plainCode);
        entity.setCodeHash(encryptionService.hash(plainCode));
        entity.setFingerprintHash(encryptionService.hash(fingerprint));
        entity.setDeviceInfo(deviceInfo);
        entity.setExpiresAt(LocalDateTime.now().plusHours(24));


        return codeRepo.save(entity);
    }

    public DeviceToken verifyRecoveryCode(String code, String newFingerprint) {

        String codeHash = encryptionService.hash(code.trim());
        System.out.println("Searching for code hash: " + codeHash);

        // 修正查询方式
        RecoveryCode record = codeRepo.findByCodeHash(codeHash)
                .orElseThrow(() -> {
                    System.out.println("Code not found for hash: " + codeHash);
                    return new InvalidCodeException();
                });

        // 检查是否已使用
        if (record.isUsed()) {
            System.out.println("Code already used: " + codeHash);
            throw new InvalidCodeException();
        }

        // 检查是否过期
        if (record.getExpiresAt().isBefore(LocalDateTime.now())) {
            System.out.println("Code expired: " + codeHash);
            throw new ExpiredCodeException();
        }
        // 生成设备令牌
        DeviceToken token = new DeviceToken();
        token.setTokenValue(UUID.randomUUID().toString());
        token.setFingerprintHash(encryptionService.hash(newFingerprint));
        token.setExpiresAt(LocalDateTime.now().plusDays(30));

        // 标记找回码已使用
        record.setUsed(true);

        return deviceRepo.save(token);
    }

    private String generateSecureCode() {
        // 8位随机字母数字，排除易混淆字符
        String chars = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
        SecureRandom random = new SecureRandom();
        return IntStream.range(0, 8)
                .map(i -> random.nextInt(chars.length()))
                .mapToObj(chars::charAt)
                .collect(StringBuilder::new, StringBuilder::append, StringBuilder::append)
                .toString();
    }

    public DeviceToken createGuestToken(String newFingerprint) {
        if (newFingerprint == null || newFingerprint.isBlank()) {
            throw new IllegalArgumentException("Fingerprint required");
        }

        // 1) 先算指纹哈希
        String fingerprintHash = encryptionService.hash(newFingerprint);

        // 2) 查是否已有未过期 token
        LocalDateTime now = LocalDateTime.now();
        Optional<DeviceToken> existing = deviceRepo
                .findFirstByFingerprintHashAndExpiresAtAfterOrderByExpiresAtDesc(fingerprintHash, now);

        if (existing.isPresent()) {
            // 直接返回已有 token
            return existing.get();
        }

        // 3) 不存在则创建新 token
        DeviceToken token = new DeviceToken();
        token.setTokenValue(UUID.randomUUID().toString());
        token.setFingerprintHash(fingerprintHash);
        token.setExpiresAt(now.plusDays(30));
        // token.setUserId(guestUserId);

        return deviceRepo.save(token);
    }
}