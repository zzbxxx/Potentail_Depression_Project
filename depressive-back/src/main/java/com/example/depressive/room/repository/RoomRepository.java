package com.example.depressive.room.repository;

import com.example.depressive.room.entity.Room;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RoomRepository extends JpaRepository<Room, Long> {
    boolean existsByName(String name);

    List<Room> findByStatus(Room.RoomStatus roomStatus);

    boolean existsByNameAndStatus(String name, Room.RoomStatus roomStatus);
}