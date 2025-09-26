package com.example.depressive.mood.repository;

import com.example.depressive.mood.entity.UserCardsLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface UserCardsLogRepository extends JpaRepository<UserCardsLog, Long> {
    Optional<UserCardsLog> findByUserIdAndDate(Long userId, LocalDate date);
    List<UserCardsLog> findByUserIdAndContentId(Long userId, Long contentId);
    Optional<UserCardsLog> findTopByUserIdAndContentIdOrderByDateDesc(Long userId, Short contentId);
    List<UserCardsLog> findByUserIdOrderByDateDesc(Long userId);

}