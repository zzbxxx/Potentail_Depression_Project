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

    public DeviceToken verifyRecoveryCode(String code, String newFingerprint) {

        String codeHash = encryptionService.hash(code.trim());

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

    public DeviceToken createGuestToken(String newFingerprint, String deviceInfo) {
        if (newFingerprint == null || newFingerprint.isBlank()) {
            throw new IllegalArgumentException("Fingerprint required");
        }

        // 1) 先算指纹哈希
        String fingerprintHash = encryptionService.hash(newFingerprint);
        System.out.println("Fingerprint hash: " + fingerprintHash);

        // 2) 查是否已有未过期 token
        LocalDateTime now = LocalDateTime.now();
        Optional<DeviceToken> existing = deviceRepo
                .findFirstByFingerprintHashAndExpiresAtAfterOrderByExpiresAtDesc(fingerprintHash, now);

        if (existing.isPresent()) {
            // 直接返回已有 token（包含 userId）
            return existing.get();
        }

        // 3) 创建游客用户
        User guestUser = createGuestUser(deviceInfo); // 创建游客用户
        System.out.println("guest:"+guestUser);
        // 4) 创建设备 token 并关联用户ID
        DeviceToken token = new DeviceToken();
        token.setTokenValue(UUID.randomUUID().toString());
        token.setFingerprintHash(fingerprintHash);
        token.setExpiresAt(now.plusDays(30));
        token.setUserId(guestUser.getId()); // 关联用户ID
        token.setDeviceInfo(deviceInfo);
        return deviceRepo.save(token);
    }

    // 创建游客用户的辅助方法
    private User createGuestUser(String deviceInfo) {
        User user = new User();
        user.setIsGuest(true);
        user.setUsername("Guest_" + generateGuestUsernameSuffix());
        user.setIsGuest(false);
        user.setDeviceInfo(deviceInfo);
        // 显式刷新以确保获取生成的ID
        user = userRepository.saveAndFlush(user);

        System.out.println("用户创建成功，ID: " + user.getId());
        return user;
    }
    private String generateGuestUsernameSuffix() {
        return UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }
}