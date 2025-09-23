package com.example.depressive.notification.service;

import com.example.depressive.login.entity.User;
import com.example.depressive.login.repository.UserRepository;
import com.example.depressive.notification.dto.NotificationDTO;
import com.example.depressive.notification.entity.Notification;
import com.example.depressive.notification.repository.NotificationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
public class NotificationService {

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    private static final String UNREAD_KEY_PREFIX = "notifications:unread:user:";
    private static final String READ_KEY_PREFIX = "notifications:read:user:";
    private static final String ALL_KEY_PREFIX = "notifications:all:user:";
    private static final long CACHE_TTL_MINUTES = 10;

    public List<NotificationDTO> getUnreadNotificationsByUserId(Long userId) {
        String cacheKey = UNREAD_KEY_PREFIX + userId;
        try {
            List<NotificationDTO> cachedNotifications = (List<NotificationDTO>) redisTemplate.opsForValue().get(cacheKey);
            if (cachedNotifications != null) {
                return cachedNotifications;
            }
        } catch (Exception e) {
            System.err.println("Redis cache error for unread notifications: " + e.getMessage());
        }

        List<NotificationDTO> notifications = notificationRepository
                .findByUserIdAndReadFalseOrderByTimestampDesc(userId)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());

        try {
            redisTemplate.opsForValue().set(cacheKey, notifications, CACHE_TTL_MINUTES, TimeUnit.MINUTES);
        } catch (Exception e) {
            System.err.println("Redis set cache error for unread notifications: " + e.getMessage());
        }
        return notifications;
    }

    public List<NotificationDTO> getReadNotificationsByUserId(Long userId) {
        String cacheKey = READ_KEY_PREFIX + userId;
        try {
            List<NotificationDTO> cachedNotifications = (List<NotificationDTO>) redisTemplate.opsForValue().get(cacheKey);
            if (cachedNotifications != null) {
                return cachedNotifications;
            }
        } catch (Exception e) {
            System.err.println("Redis cache error for read notifications: " + e.getMessage());
        }

        List<NotificationDTO> notifications = notificationRepository
                .findByUserIdAndReadTrueOrderByTimestampDesc(userId)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());

        try {
            redisTemplate.opsForValue().set(cacheKey, notifications, CACHE_TTL_MINUTES, TimeUnit.MINUTES);
        } catch (Exception e) {
            System.err.println("Redis set cache error for read notifications: " + e.getMessage());
        }
        return notifications;
    }

    public List<NotificationDTO> getNotificationsByUserId(Long userId) {
        String cacheKey = ALL_KEY_PREFIX + userId;
        try {
            List<NotificationDTO> cachedNotifications = (List<NotificationDTO>) redisTemplate.opsForValue().get(cacheKey);
            if (cachedNotifications != null) {
                return cachedNotifications;
            }
        } catch (Exception e) {
            System.err.println("Redis cache error for all notifications: " + e.getMessage());
        }

        List<NotificationDTO> notifications = notificationRepository
                .findByUserIdOrderByTimestampDesc(userId)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());

        try {
            redisTemplate.opsForValue().set(cacheKey, notifications, CACHE_TTL_MINUTES, TimeUnit.MINUTES);
        } catch (Exception e) {
            System.err.println("Redis set cache error for all notifications: " + e.getMessage());
        }
        return notifications;
    }

    public Notification createNotification(Long userId, String title, String content, String notificationType) {
        Notification notification = new Notification();
        Optional<User> userOptional = userRepository.findById(userId);
        User user = userOptional.orElseThrow(() -> new RuntimeException("用户不存在"));
        notification.setUser(user);
        notification.setTitle(title);
        notification.setContent(content);
        notification.setNotificationType(notificationType != null ? notificationType : "GENERAL");
        notification.setTimestamp(new Date());
        notification.setRead(false);
        Notification savedNotification = notificationRepository.save(notification);

        try {
            redisTemplate.delete(UNREAD_KEY_PREFIX + userId);
            redisTemplate.delete(ALL_KEY_PREFIX + userId);
        } catch (Exception e) {
            System.err.println("Redis delete cache error on create notification: " + e.getMessage());
        }
        return savedNotification;
    }

    public void markAsRead(Long id) {
        Notification notification = notificationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("通知不存在"));
        notification.setRead(true);
        notificationRepository.save(notification);

        try {
            Long userId = notification.getUser().getId();
            redisTemplate.delete(UNREAD_KEY_PREFIX + userId);
            redisTemplate.delete(READ_KEY_PREFIX + userId);
            redisTemplate.delete(ALL_KEY_PREFIX + userId);
        } catch (Exception e) {
            System.err.println("Redis delete cache error on mark as read: " + e.getMessage());
        }
    }

    private NotificationDTO convertToDTO(Notification notification) {
        NotificationDTO dto = new NotificationDTO();
        dto.setId(notification.getId());
        dto.setUserId(notification.getUser().getId());
        dto.setTitle(notification.getTitle());
        dto.setContent(notification.getContent());
        dto.setNotificationType(notification.getNotificationType());
        dto.setTimestamp(notification.getTimestamp());
        dto.setRead(notification.isRead());
        return dto;
    }
}
