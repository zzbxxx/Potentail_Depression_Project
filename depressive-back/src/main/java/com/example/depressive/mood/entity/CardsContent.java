package com.example.depressive.mood.entity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "cards_content")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class CardsContent {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Short id;
    private String quoteText;
    private String author;
    private String bookTitle;
    @Column(columnDefinition = "json")
    private String tags;
    private Boolean isActive = true;
}
