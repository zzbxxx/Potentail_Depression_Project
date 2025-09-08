package com.example.depressive.login.repository;

import com.example.depressive.login.entity.DeviceToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.Optional;

public interface DeviceRepository extends JpaRepository<DeviceToken, Long> {
    void deleteByExpiresAtBefore(LocalDateTime dateTime);

    Optional<DeviceToken> findFirstByFingerprintHashAndExpiresAtAfterOrderByExpiresAtDesc(String fingerprintHash, LocalDateTime now);
}