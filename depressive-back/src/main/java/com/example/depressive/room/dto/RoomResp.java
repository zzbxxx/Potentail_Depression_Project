package com.example.depressive.room.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Getter
@Setter
public class RoomResp {
    private Long id;
    private String name;
    private String type;
    private Set<String> topics;
    private Byte maxUsers;
    private Byte currentUsers;
    private Long creatorId;
    private String creatorNickname;
    private String creatorAvatar; // 從 users.avatar 獲取
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime endTime;
    private String avatar;
    private List<Map<String, Object>> users;
}