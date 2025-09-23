package com.example.depressive.notification.websocket;

import com.example.depressive.notification.entity.Notification;
import com.example.depressive.notification.repository.NotificationRepository; // 新增依赖
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class NotificationWebSocketHandler extends TextWebSocketHandler {
    @Autowired
    private NotificationRepository notificationRepository; // 替换 NotificationService

    // 存储用户与 WebSocket 会话的映射
    private static final Map<Long, WebSocketSession> sessions = new ConcurrentHashMap<>();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        Long userId = getUserIdFromSession(session);
        sessions.put(userId, session);
        // 发送历史通知
        sendInitialNotifications(userId, session);
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        Long userId = getUserIdFromSession(session);
        sessions.remove(userId);
    }

    // 推送新通知
    public void sendNotification(Long userId, Notification notification) {
        WebSocketSession session = sessions.get(userId);
        if (session != null && session.isOpen()) {
            try {
                session.sendMessage(new TextMessage(notification.toString())); // 需序列化
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    // 发送初始通知
    private void sendInitialNotifications(Long userId, WebSocketSession session) {
        // 使用 NotificationRepository 获取未读通知
        var notifications = notificationRepository.findByUserIdAndRead(userId, false);
        if (!notifications.isEmpty()) {
            try {
                session.sendMessage(new TextMessage(notifications.toString())); // 简化示例，需优化序列化
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    // 自定义方法，从 session 获取 userId（需根据认证实现）
    private Long getUserIdFromSession(WebSocketSession session) {
        // 例如从 SecurityContext 或自定义属性获取
        return 1L; // 临时默认值，需替换为实际逻辑
    }
}