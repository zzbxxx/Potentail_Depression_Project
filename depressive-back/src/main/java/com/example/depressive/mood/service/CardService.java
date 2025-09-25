package com.example.depressive.mood.service;

import com.example.depressive.mood.dto.CardResp;
import com.example.depressive.mood.entity.CardsContent;
import com.example.depressive.mood.entity.UserCardsLog;
import com.example.depressive.mood.entity.UserMoodLog;
import com.example.depressive.mood.repository.CardsContentRepository;
import com.example.depressive.mood.repository.UserCardsLogRepository;
import com.example.depressive.mood.repository.UserMoodLogRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.*;
import java.util.*;

@Service
@RequiredArgsConstructor
public class CardService {
    private final CardsContentRepository cardRepo;
    private final UserCardsLogRepository logRepo;
    private final UserMoodLogRepository moodRepo;
    private final RedisTemplate<String, Object> redis;

    public CardResp todayCard(Long userId, boolean isGuest) {
        String key = "user:" + userId + ":card:recommendation" + (isGuest ? ":guest" : ":user");
        try {
            CardResp cached = (CardResp) redis.opsForValue().get(key);
            if (cached != null) return cached;
        } catch (Exception e) {
            System.err.println("Redis cache error: " + e.getMessage());
        }

        LocalDate today = LocalDate.now();
        if (logRepo.findByUserIdAndDate(userId, today).isPresent()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "今日已领取");
        }

        List<CardsContent> active = cardRepo.findByIsActiveTrue();
        if (active.isEmpty()) throw new ResponseStatusException(HttpStatus.NOT_FOUND, "无可用卡片");

        CardsContent selectedCard;
        if (!moodRepo.existsByUserId(userId)) {
            selectedCard = active.get(new Random().nextInt(active.size()));
        } else {
            selectedCard = recommendCardBasedOnMood(userId, active);
        }

        CardResp resp = new CardResp();
        BeanUtils.copyProperties(selectedCard, resp);
        resp.setTags(List.of(selectedCard.getTags().replaceAll("[\\[\\]\"]", "").split(",")));

        long seconds = LocalDateTime.of(today.plusDays(1), LocalTime.MIN)
                .toEpochSecond(ZoneOffset.ofHours(8)) - Instant.now().getEpochSecond();
        redis.opsForValue().set(key, resp, Duration.ofSeconds(seconds));

        UserCardsLog log = new UserCardsLog(null, userId, today, selectedCard.getId());
        logRepo.save(log);

        return resp;
    }

    public List<CardResp> history(Long userId) {
        return logRepo.findByUserIdOrderByDateDesc(userId)
                .stream()
                .map(l -> {
                    CardsContent c = cardRepo.findById(l.getContentId()).orElseThrow();
                    CardResp r = new CardResp();
                    BeanUtils.copyProperties(c, r);
                    r.setTags(List.of(c.getTags().replaceAll("[\\[\\]\"]", "").split(",")));
                    r.setDate(l.getDate());
                    System.out.println("l is" + l.getDate());
                    return r;
                })
                .toList();
    }

    public CardResp getCardByCardId(Long userId, Short contentId) {
        // 查詢卡片數據
        CardsContent card = cardRepo.findById(contentId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "卡片不存在"));

        // 查詢用戶卡片日誌（取最新一條記錄）
        Optional<UserCardsLog> logOptional = logRepo.findTopByUserIdAndContentIdOrderByDateDesc(userId, contentId);

        // 封裝返回數據
        CardResp resp = new CardResp();
        BeanUtils.copyProperties(card, resp);
        resp.setTags(List.of(card.getTags().replaceAll("[\\[\\]\"]", "").split(",")));
        resp.setDate(logOptional.map(UserCardsLog::getDate).orElse(null));

        return resp;
    }

    private CardsContent recommendCardBasedOnMood(Long userId, List<CardsContent> activeCards) {
        List<UserMoodLog> recentMoods = moodRepo.findTop5ByUserIdOrderByDateDesc(userId);
        if (recentMoods.isEmpty()) {
            return activeCards.get(new Random().nextInt(activeCards.size()));
        }

        Map<String, Double> averageMood = calculateAverageMood(recentMoods);
        String dominantMood = identifyDominantMood(averageMood);

        CardsContent recommendedCard = findMatchingCard(activeCards, dominantMood, averageMood);
        return recommendedCard != null ? recommendedCard :
                activeCards.get(new Random().nextInt(activeCards.size()));
    }

    private Map<String, Double> calculateAverageMood(List<UserMoodLog> moodLogs) {
        Map<String, Double> sumMap = new HashMap<>();
        Map<String, Integer> countMap = new HashMap<>();

        for (UserMoodLog log : moodLogs) {
            try {
                Map<String, Double> moodVector = new ObjectMapper()
                        .readValue(log.getMoodVector(), new TypeReference<Map<String, Double>>() {});
                for (Map.Entry<String, Double> entry : moodVector.entrySet()) {
                    sumMap.put(entry.getKey(), sumMap.getOrDefault(entry.getKey(), 0.0) + entry.getValue());
                    countMap.put(entry.getKey(), countMap.getOrDefault(entry.getKey(), 0) + 1);
                }
            } catch (Exception e) {
                System.err.println("Error parsing mood vector: " + e.getMessage());
            }
        }

        Map<String, Double> averageMap = new HashMap<>();
        for (String key : sumMap.keySet()) {
            averageMap.put(key, sumMap.get(key) / countMap.get(key));
        }
        return averageMap;
    }

    private String identifyDominantMood(Map<String, Double> moodVector) {
        return moodVector.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse("calm");
    }

    private CardsContent findMatchingCard(List<CardsContent> cards, String dominantMood, Map<String, Double> moodVector) {
        List<CardsContent> matchingCards = new ArrayList<>();
        for (CardsContent card : cards) {
            String tags = card.getTags().replaceAll("[\\[\\]\"]", "");
            if (shouldRecommendForMood(tags, dominantMood, moodVector)) {
                matchingCards.add(card);
            }
        }
        return !matchingCards.isEmpty() ? matchingCards.get(new Random().nextInt(matchingCards.size())) : null;
    }

    private boolean shouldRecommendForMood(String tags, String dominantMood, Map<String, Double> moodVector) {
        return switch (dominantMood) {
            case "sad" -> tags.contains("hopeful") || tags.contains("encouraging") || tags.contains("comforting");
            case "anxious" -> tags.contains("calm") || tags.contains("peaceful") || tags.contains("mindful");
            case "happy" -> tags.contains("inspiring") || tags.contains("wise") || tags.contains("progress");
            case "calm" -> tags.contains("calm") || tags.contains("peaceful") || tags.contains("introspective");
            default -> true;
        };
    }
}