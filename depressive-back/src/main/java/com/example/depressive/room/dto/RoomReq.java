package com.example.depressive.room.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.Set;

@Getter
@Setter
public class RoomReq {
    private Long userId; // 創建者 ID
    private String name; // 房間名稱
    private String type; // 房間類型
    private Set<String> topics; // 標籤（最多 3 個）
    private Byte maxUsers; // 最大人數
    private String avatar; // 房間頭像 URL
}