package com.example.depressive.room.entity;

import java.io.Serializable;
import java.util.Objects;

public class RoomTopicId implements Serializable {
    private Long room; // 對應 Room 的 ID
    private Long topic; // 對應 Topic 的 ID

    // 必須提供無參構造函數
    public RoomTopicId() {}

    public RoomTopicId(Long room, Long topic) {
        this.room = room;
        this.topic = topic;
    }

    // 必須實現 equals 和 hashCode
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RoomTopicId that = (RoomTopicId) o;
        return Objects.equals(room, that.room) && Objects.equals(topic, that.topic);
    }

    @Override
    public int hashCode() {
        return Objects.hash(room, topic);
    }
}