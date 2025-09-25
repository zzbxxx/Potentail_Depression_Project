package com.example.depressive.mood.entity;
import jakarta.persistence.*;
import lombok.*;
@Entity @Table(name = "user_cards_log")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class UserCardsLog {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Long userId;
    private java.time.LocalDate date;
    private short contentId;
}
