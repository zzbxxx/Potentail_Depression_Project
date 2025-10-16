package com.example.depressive.room.controller;

import com.example.depressive.room.service.RoomService;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;

import java.util.Map;

@Controller
public class RoomWebSocketController {

    @Autowired
    private RoomService roomService;
    @MessageMapping("/room/{roomId}/updateAvatar")
    @SendTo("/topic/room/{roomId}/avatarUpdate")
    public Map<String, Object> updateAvatar(@DestinationVariable Long roomId,
                                            @Payload Map<String, Object> payload) {
        // 这里可以添加额外逻辑，如验证 payload，但简单广播即可
        return payload; // 直接广播给订阅者
    }

    // 座位變更
    @MessageMapping("/room/{roomId}/seatChange")
    @SendTo("/topic/room/{roomId}/seatUpdate")  // 如果需要座位变更广播
    public Map<String, Object> seatChange(@DestinationVariable Long roomId,
                                          @Payload Map<String, Object> payload) throws JsonProcessingException {
        Long userId = Long.parseLong(payload.get("userId").toString());
        int seatIndex = Integer.parseInt(payload.get("seatIndex").toString());

        // 更新後端 Redis 座位
        roomService.updateSeat(roomId, userId, seatIndex);

        System.out.println("🪑 收到座位變更: " + payload);
        return payload;
    }

    @MessageMapping("/room/{roomId}/updateState")
    @SendTo("/topic/room/{roomId}/states")
    public Map<String, Object> updateState(@DestinationVariable Long roomId,
                                           @Payload Map<String, Object> payload) {
        return payload;
    }

    @MessageMapping("/room/{roomId}/message")
    @SendTo("/topic/room/{roomId}/messages")
    public Map<String, Object> message(@DestinationVariable Long roomId,
                                       @Payload Map<String, Object> payload) {
        return payload;
    }
}