package com.example.depressive.mood.service;

import com.example.depressive.mood.dto.MoodReq;
import com.example.depressive.mood.entity.UserMoodLog;
import com.example.depressive.mood.repository.UserMoodLogRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.*;
import java.util.*;

@Service
@RequiredArgsConstructor
public class MoodService {
    private final UserMoodLogRepository moodRepo;

    public void saveMood(MoodReq req) throws JsonProcessingException {
        LocalDate today = LocalDate.now();
        if (moodRepo.existsByUserIdAndDate(req.getUserId(), today)) return;

        // 计算 primaryMood（支持多种，例如强度最高的前 2 个）
        List<String> primaryMoods = calculatePrimaryMoods(req.getMoodVector());

        UserMoodLog log = new UserMoodLog();
        log.setUserId(req.getUserId());
        log.setDate(today);
        log.setMoodVector(new ObjectMapper().writeValueAsString(req.getMoodVector()));
        log.setPrimaryMood(new ObjectMapper().writeValueAsString(primaryMoods)); // 存储为 JSON 数组字符串
        log.setEvents(new ObjectMapper().writeValueAsString(req.getEvents()));
        log.setText(req.getText());
        moodRepo.save(log);
    }

    // 新增方法：计算多种主导情绪（例如强度最高的前 2 个）
    private List<String> calculatePrimaryMoods(Map<String, Double> moodVector) {
        if (moodVector == null || moodVector.isEmpty()) {
            return List.of("calm"); // 默认
        }

        // 按强度降序排序，取前 2 个（或根据需求调整阈值，例如 > 0.5 的所有）
        return moodVector.entrySet().stream()
                .sorted((a, b) -> Double.compare(b.getValue(), a.getValue()))
                .limit(2) // 支持多种，取前 2 个
                .map(Map.Entry::getKey)
                .toList();
    }

}