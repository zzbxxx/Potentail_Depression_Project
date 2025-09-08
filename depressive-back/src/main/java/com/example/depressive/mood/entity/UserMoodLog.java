package com.example.depressive.mood.entity;
import jakarta.persistence.*;
import lombok.*;
// UserMoodLog.java
@Entity @Table(name = "user_mood_log")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class UserMoodLog {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Long userId;
    private java.time.LocalDate date;
    @Column(columnDefinition = "json")
    private String moodVector;
    private String primaryMood;
}
