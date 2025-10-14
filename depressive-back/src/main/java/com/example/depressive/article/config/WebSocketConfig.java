package com.example.depressive.article.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        // 啟用簡單的消息代理，處理以 /topic 開頭的訂閱
        config.enableSimpleBroker("/topic");
        // 設置應用程序前綴，客戶端發送的消息需要以 /app 開頭
        config.setApplicationDestinationPrefixes("/app");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // 註冊 STOMP 端點，與前端的 SockJS 連接匹配
        registry.addEndpoint("/ws")
                .setAllowedOrigins("http://localhost:5173") // 限制為前端域名
                .withSockJS();
    }
}