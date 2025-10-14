package com.example.depressive.room.entity;

import com.example.depressive.login.entity.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "rooms")
@Getter
@Setter
@NoArgsConstructor
public class Room {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", nullable = false, unique = true, length = 50)
    private String name;

    @Column(name = "type", nullable = false)
    @Enumerated(EnumType.STRING)
    private RoomType type;

    @Column(name = "max_users", nullable = false)
    private Byte maxUsers;

    @Column(name = "current_users")
    private Byte currentUsers = 0;

    @ManyToOne
    @JoinColumn(name = "creator_id", nullable = false)
    private User creator;

    @Column(name = "status")
    @Enumerated(EnumType.STRING)
    private RoomStatus status = RoomStatus.active;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at")
    private LocalDateTime updatedAt = LocalDateTime.now();

    @Column(name = "end_time")
    private LocalDateTime endTime;

    @Column(name = "avatar", length = 255)
    private String avatar = "";


    public enum RoomType {
        strict_tomato, free_time, mutual_aid, programming
    }

    public enum RoomStatus {
        active, closed
    }
}