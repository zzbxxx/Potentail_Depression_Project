package com.example.depressive.mood.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.Map;

@Data
public class MoodReq {
    @NotNull
    private Long userId;
    private Map<String, Double> moodVector;
    private String primaryMood;
}
