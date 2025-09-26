package com.example.depressive.mood.dto;

import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
public class CardResp {
    private Short id;
    private String quoteText;
    private String author;
    private String bookTitle;
    private List<String> tags;
    private LocalDate date;
    private Boolean isFavorited;
}
