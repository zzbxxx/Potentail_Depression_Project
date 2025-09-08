package com.example.depressive.mood.repository;

import com.example.depressive.mood.entity.UserMoodLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface UserMoodLogRepository extends JpaRepository<UserMoodLog, Long> {
    Optional<UserMoodLog> findTopByUserIdOrderByDateDesc(Long userId);
    boolean existsByUserIdAndDate(Long userId, LocalDate date);

    List<UserMoodLog> findTop5ByUserIdOrderByDateDesc(Long userId);

    boolean existsByUserId(Long userId);
}
