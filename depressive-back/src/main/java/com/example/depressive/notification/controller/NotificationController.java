package com.example.depressive.notification.controller;

import com.example.depressive.notification.dto.NotificationDTO;
import com.example.depressive.notification.entity.Notification;
import com.example.depressive.notification.service.NotificationService;
import com.example.depressive.notification.websocket.NotificationWebSocketHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private NotificationWebSocketHandler webSocketHandler;

    @PostMapping
    public ResponseEntity<Notification> createNotification(@RequestBody NotificationDTO request) {
        System.out.println("notify:"+request);
        Notification notification = notificationService.createNotification(
                request.getUserId(),
                request.getTitle(),
                request.getContent(),
                request.getNotificationType(),
                request.getTriggerUserId()
        );
        webSocketHandler.sendNotification(notification.getUser().getId(), notification);
        return ResponseEntity.ok(notification);
    }

    @GetMapping("/user/{userId}/unread")
    public ResponseEntity<List<NotificationDTO>> getUnreadUserNotifications(@PathVariable Long userId) {
        List<NotificationDTO> notifications = notificationService.getUnreadNotificationsByUserId(userId);
        return ResponseEntity.ok(notifications);
    }

    @GetMapping("/user/{userId}/read")
    public ResponseEntity<List<NotificationDTO>> getReadUserNotifications(@PathVariable Long userId) {
        List<NotificationDTO> notifications = notificationService.getReadNotificationsByUserId(userId);
        return ResponseEntity.ok(notifications);
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<NotificationDTO>> getUserNotifications(@PathVariable Long userId) {
        List<NotificationDTO> notifications = notificationService.getNotificationsByUserId(userId);
        return ResponseEntity.ok(notifications);
    }

    @PutMapping("/{id}/read")
    public ResponseEntity<Void> markAsRead(@PathVariable Long id) {
        notificationService.markAsRead(id);
        return ResponseEntity.ok().build();
    }
}
