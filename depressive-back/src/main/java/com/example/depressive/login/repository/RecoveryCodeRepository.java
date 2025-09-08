package com.example.depressive.login.repository;

import com.example.depressive.login.entity.RecoveryCode;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.Optional;

public interface RecoveryCodeRepository extends JpaRepository<RecoveryCode, Long> {
    Optional<RecoveryCode> findByCodeHash(String codeHash);

    void deleteByExpiresAtBefore(LocalDateTime dateTime);
}