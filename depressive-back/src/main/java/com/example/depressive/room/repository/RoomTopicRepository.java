package com.example.depressive.room.repository;

import com.example.depressive.room.entity.RoomTopic;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RoomTopicRepository extends JpaRepository<RoomTopic, Long> {
    List<RoomTopic> findByRoomId(Long id);
}