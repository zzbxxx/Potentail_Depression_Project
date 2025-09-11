package com.example.depressive.mood.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class MoodReq {
    @NotNull
    private Long userId;
    private Map<String, Double> moodVector;
    private List<String> events;
    private String text;
}  
