package com.example.depressive.login.service;

import com.example.depressive.login.entity.DeviceToken;
import com.example.depressive.login.entity.RecoveryCode;
import com.example.depressive.login.entity.User;
import com.example.depressive.login.exception.ExpiredCodeException;
import com.example.depressive.login.exception.InvalidCodeException;
import com.example.depressive.login.repository.DeviceRepository;
import com.example.depressive.login.repository.RecoveryCodeRepository;
import com.example.depressive.login.repository.UserRepository;
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
    private final UserRepository userRepository;

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

    public DeviceToken verifyRecoveryCode(String code, String newFingerprint, String deviceInfo) {
        System.out.println("all par:" + code + newFingerprint + deviceInfo);
        String codeHash = encryptionService.hash(code.trim());

        RecoveryCode record = codeRepo.findByCodeHash(codeHash)
                .orElseThrow(() -> {
                    System.out.println("Code not found for hash: " + codeHash);
                    return new InvalidCodeException();
                });

        if (record.isUsed()) {
            System.out.println("Code already used: " + codeHash);
            throw new InvalidCodeException();
        }

        if (record.getExpiresAt().isBefore(LocalDateTime.now())) {
            System.out.println("Code expired: " + codeHash);
            throw new ExpiredCodeException();
        }

        // 创建游客用户
        User guestUser = createGuestUser(deviceInfo != null ? deviceInfo : "Unknown Device");
        System.out.println("Created guest user with ID: " + guestUser.getId());

        // 生成设备令牌
        DeviceToken token = new DeviceToken();
        token.setTokenValue(UUID.randomUUID().toString());
        token.setFingerprintHash(encryptionService.hash(newFingerprint));
        token.setExpiresAt(LocalDateTime.now().plusDays(30));
        token.setUserId(guestUser.getId());
        token.setDeviceInfo(deviceInfo != null ? deviceInfo : "Unknown Device");

        // 标记找回码已使用
        record.setUsed(true);

        return deviceRepo.save(token);
    }

    private String generateSecureCode() {
        String chars = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
        SecureRandom random = new SecureRandom();
        return IntStream.range(0, 8)
                .map(i -> random.nextInt(chars.length()))
                .mapToObj(chars::charAt)
                .collect(StringBuilder::new, StringBuilder::append, StringBuilder::append)
                .toString();
    }

    public DeviceToken createGuestToken(String newFingerprint, String deviceInfo) {
        try {
            if (newFingerprint == null || newFingerprint.isBlank()) {
                throw new IllegalArgumentException("Fingerprint required");
            }

            String safeDeviceInfo = deviceInfo != null ? deviceInfo : "Unknown Device";
            String fingerprintHash = encryptionService.hash(newFingerprint);

            System.out.println("Fingerprint hash: " + fingerprintHash);

            LocalDateTime now = LocalDateTime.now();
            Optional<DeviceToken> existing = deviceRepo
                    .findFirstByFingerprintHashAndExpiresAtAfterOrderByExpiresAtDesc(fingerprintHash, now);

            if (existing.isPresent()) {
                return existing.get();
            }

            User guestUser = createGuestUser(safeDeviceInfo);
            System.out.println("Created guest user with ID: " + guestUser.getId());

            if (guestUser.getId() == null) {
                throw new IllegalStateException("Failed to create guest user - ID is null");
            }

            DeviceToken token = new DeviceToken();
            token.setTokenValue(UUID.randomUUID().toString());
            token.setFingerprintHash(fingerprintHash);
            token.setExpiresAt(now.plusDays(30));
            token.setUserId(guestUser.getId());
            token.setDeviceInfo(safeDeviceInfo);

            return deviceRepo.save(token);
        } catch (Exception e) {
            System.err.println("Error creating guest token: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }

    private User createGuestUser(String deviceInfo) {
        User user = new User();
        user.setIsGuest(true);
        user.setUsername("Guest_" + generateGuestUsernameSuffix());
        user.setDeviceInfo(deviceInfo);
        user = userRepository.saveAndFlush(user);
        System.out.println("用户创建成功，ID: " + user.getId());
        return user;
    }

    private String generateGuestUsernameSuffix() {
        return UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }
}