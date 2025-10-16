package com.example.depressive.room.controller;

import com.example.depressive.room.dto.RoomReq;
import com.example.depressive.room.dto.RoomResp;
import com.example.depressive.room.entity.Room;
import com.example.depressive.room.repository.RoomRepository;
import com.example.depressive.room.service.RoomService;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.bind.annotation.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@RestController
@RequestMapping("/api/rooms")
@CrossOrigin(origins = "http://localhost:5173", allowCredentials = "true")
public class RoomController {

    @Autowired
    private RoomService roomService;

    @Autowired
    private RoomRepository roomRepository;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @PostMapping("/createRoom")
    public ResponseEntity<Map<String, Object>> createRoom(@RequestBody RoomReq roomReq) {
        Map<String, Object> response = new HashMap<>();
        try {
            // 檢查用戶是否已在其他活躍房間
            RoomResp currentRoom = roomService.getCurrentRoom(roomReq.getUserId());
            if (currentRoom != null) {
                response.put("success", false);
                response.put("message", "創建自習室失敗: 用戶已在房間 " + currentRoom.getId() + " 中");
                return ResponseEntity.badRequest().body(response);
            }
            RoomResp roomResp = roomService.createRoom(roomReq);
            response.put("success", true);
            response.put("message", "自習室創建成功");
            response.put("data", roomResp);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            response.put("success", false);
            response.put("message", "創建自習室失敗: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "服務器錯誤: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }

    @GetMapping("/active")
    public ResponseEntity<Map<String, Object>> getActiveRooms() {
        Map<String, Object> response = new HashMap<>();
        try {
            List<RoomResp> rooms = roomService.getActiveRooms();
            response.put("success", true);
            response.put("message", "查詢活躍自習室成功");
            response.put("data", rooms);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "查詢活躍自習室失敗: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }

    @GetMapping("/{roomId}")
    public ResponseEntity<Map<String, Object>> getRoomById(@PathVariable Long roomId) {
        Map<String, Object> response = new HashMap<>();
        try {
            RoomResp roomResp = roomService.getRoomById(roomId);
            response.put("success", true);
            response.put("message", "查詢自習室成功");
            response.put("data", roomResp);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            response.put("success", false);
            response.put("message", "查詢自習室失敗: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "服務器錯誤: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }

    @PostMapping("/join/{roomId}")
    public ResponseEntity<Map<String, Object>> joinRoom(@PathVariable Long roomId, @RequestBody Map<String, Long> request) {
        Map<String, Object> response = new HashMap<>();
        try {
            Long userId = request.get("userId");
            RoomResp roomResp = roomService.joinRoom(roomId, userId);
            response.put("success", true);
            response.put("message", "加入自習室成功");
            response.put("data", roomResp);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            response.put("success", false);
            response.put("message", "加入自習室失敗: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "服務器錯誤: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }

    @PostMapping("/leave/{roomId}")
    public ResponseEntity<Map<String, Object>> leaveRoom(@PathVariable Long roomId, @RequestBody Map<String, Long> request) {
        Map<String, Object> response = new HashMap<>();
        try {
            Long userId = request.get("userId");
            RoomResp roomResp = roomService.leaveRoom(roomId, userId);
            response.put("success", true);
            response.put("message", "退出自習室成功");
            response.put("data", roomResp);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            response.put("success", false);
            response.put("message", "退出自習室失敗: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "服務器錯誤: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }

    @GetMapping("/current/{userId}")
    public ResponseEntity<Map<String, Object>> getCurrentRoom(@PathVariable Long userId) {
        Map<String, Object> response = new HashMap<>();
        try {
            RoomResp roomResp = roomService.getCurrentRoom(userId);
            response.put("success", true);
            response.put("message", roomResp != null ? "查詢當前房間成功" : "用戶不在任何房間");
            response.put("data", roomResp);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            response.put("success", false);
            response.put("message", "查詢當前房間失敗: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "服務器錯誤: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }

    @GetMapping("/my-room/{userId}")
    public ResponseEntity<Map<String, Object>> getMyRoom(@PathVariable Long userId) {
        Map<String, Object> response = new HashMap<>();
        try {
            RoomResp roomResp = roomService.getCurrentRoom(userId);
            response.put("success", true);
            response.put("message", roomResp != null ? "查詢我的房間成功" : "您目前不在任何自習室中");
            response.put("data", roomResp);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            response.put("success", false);
            response.put("message", "查詢我的房間失敗: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "服務器錯誤: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }

    @PutMapping("/{roomId}/avatar")
    public ResponseEntity<Map<String, Object>> updateRoomAvatar(
            @PathVariable Long roomId,
            @RequestBody Map<String, Object> request) {
        Map<String, Object> response = new HashMap<>();
        try {
            Long userId = Long.valueOf(request.get("userId").toString());
            String avatar = (String) request.get("avatar");

            RoomResp roomResp = roomService.updateRoomAvatar(roomId, userId, avatar);
            response.put("success", true);
            response.put("message", "房間頭像更新成功");
            response.put("data", roomResp);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            response.put("success", false);
            response.put("message", "更新頭像失敗: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "服務器錯誤: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }

    @PutMapping("/{roomId}/userState")
    public ResponseEntity<Map<String, Object>> updateUserState(@PathVariable Long roomId, @RequestBody Map<String, Object> request) {
        Map<String, Object> response = new HashMap<>();
        try {
            Long userId = Long.valueOf(request.get("userId").toString());
            String status = (String) request.get("status");
            String message = (String) request.get("message");
            int timer = Integer.parseInt(request.get("timer").toString());

            roomService.updateUserState(roomId, userId, status, message, timer);
            response.put("success", true);
            response.put("message", "狀態更新成功");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "更新失敗: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @Scheduled(fixedRate = 30000) // 每 30 秒執行一次
    public void syncRoomStates() throws JsonProcessingException {
        List<Room> activeRooms = roomRepository.findByStatus(Room.RoomStatus.active);
        for (Room room : activeRooms) {
            ResponseEntity<Map<String, Object>> roomResp = getRoomById(room.getId());
            messagingTemplate.convertAndSend("/topic/room/" + room.getId() + "/fullUpdate", roomResp);
        }
    }
}