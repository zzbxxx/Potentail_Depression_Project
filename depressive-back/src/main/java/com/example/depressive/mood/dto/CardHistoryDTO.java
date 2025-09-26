package com.example.depressive.mood.dto;

import lombok.Data;

import java.time.LocalDate;

@Data
public class CardHistoryDTO {
    private Short id;
    private LocalDate date;
}