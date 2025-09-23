package com.example.depressive.notification.repository;

import com.example.depressive.notification.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {
    // 根据用户ID和已读状态查询通知
    List<Notification> findByUserIdAndRead(Long userId, boolean read);

    // 根据用户ID按时间降序查询所有通知
    List<Notification> findByUserIdOrderByTimestampDesc(Long userId);

    List<Notification> findByUserIdAndReadFalseOrderByTimestampDesc(Long userId);
    List<Notification> findByUserIdAndReadTrueOrderByTimestampDesc(Long userId);
}