package com.example.depressive.mood.service;

import com.example.depressive.mood.dto.CardResp;
import com.example.depressive.mood.dto.MoodReq;
import com.example.depressive.mood.entity.CardsContent;
import com.example.depressive.mood.entity.UserCardsLog;
import com.example.depressive.mood.entity.UserMoodLog;
import com.example.depressive.mood.repository.CardsContentRepository;
import com.example.depressive.mood.repository.UserCardsLogRepository;
import com.example.depressive.mood.repository.UserMoodLogRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
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

    public CardResp todayCard(Long userId) {
        String key = "user:" + userId + ":card:recommendation";
        CardResp cached = (CardResp) redis.opsForValue().get(key);
        System.out.println("cache:"+cached);
        if (cached != null) return cached;

        LocalDate today = LocalDate.now();
        if (logRepo.findByUserIdAndDate(userId, today).isPresent()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "今日已领取");
        }

        List<CardsContent> active = cardRepo.findByIsActiveTrue();
        System.out.println("card:"+active);
        if (active.isEmpty()) throw new ResponseStatusException(HttpStatus.NOT_FOUND, "无可用卡片");

        // 新增：检查用户是否有历史情绪记录
        boolean hasMoodHistory = moodRepo.existsByUserId(userId);
        System.out.println("大家好");
        CardsContent selectedCard;
        if (!hasMoodHistory) {
            // 没有情绪记录：随机选择
            System.out.println("无记录");
            selectedCard = active.get(new Random().nextInt(active.size()));
        } else {
            // 有情绪记录：基于算法推荐
            System.out.println("有记录");
            selectedCard = recommendCardBasedOnMood(userId, active);
        }

        CardResp resp = new CardResp();
        BeanUtils.copyProperties(selectedCard, resp);
        resp.setTags(List.of(selectedCard.getTags().replaceAll("[\\[\\]\"]", "").split(",")));

        // 缓存到今晚 23:59:59
        long seconds = LocalDateTime.of(today.plusDays(1), LocalTime.MIN)
                .toEpochSecond(ZoneOffset.ofHours(8)) - Instant.now().getEpochSecond();
        redis.opsForValue().set(key, resp, Duration.ofSeconds(seconds));

        // 记录日志
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
                    return r;
                })
                .toList();
    }

    public void saveMood(MoodReq req) throws JsonProcessingException {
        LocalDate today = LocalDate.now();
        if (moodRepo.existsByUserIdAndDate(req.getUserId(), today)) return;

        UserMoodLog log = new UserMoodLog();
        log.setUserId(req.getUserId());
        log.setDate(today);
        log.setMoodVector(new ObjectMapper().writeValueAsString(req.getMoodVector()));
        log.setPrimaryMood(req.getPrimaryMood());
        moodRepo.save(log);
    }

    private CardsContent recommendCardBasedOnMood(Long userId, List<CardsContent> activeCards) {
        // 1. 获取用户最近的情绪数据
        List<UserMoodLog> recentMoods = moodRepo.findTop5ByUserIdOrderByDateDesc(userId);
        if (recentMoods.isEmpty()) {
            return activeCards.get(new Random().nextInt(activeCards.size()));
        }

        // 2. 分析情绪趋势（这里需要你定义具体的算法）
        Map<String, Double> averageMood = calculateAverageMood(recentMoods);
        String dominantMood = identifyDominantMood(averageMood);

        // 3. 基于情绪状态匹配最合适的卡片
        // 例如：如果用户最近比较悲伤，推荐鼓舞人心的卡片
        // 如果用户比较焦虑，推荐平静舒缓的卡片
        CardsContent recommendedCard = findMatchingCard(activeCards, dominantMood, averageMood);

        return recommendedCard != null ? recommendedCard :
                activeCards.get(new Random().nextInt(activeCards.size()));
    }

    // 计算平均情绪值
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
                // 处理解析异常
            }
        }

        Map<String, Double> averageMap = new HashMap<>();
        for (String key : sumMap.keySet()) {
            averageMap.put(key, sumMap.get(key) / countMap.get(key));
        }

        return averageMap;
    }

    // 识别主导情绪
    private String identifyDominantMood(Map<String, Double> moodVector) {
        return moodVector.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse("calm"); // 默认情绪
    }

    // 根据情绪匹配卡片
    private CardsContent findMatchingCard(List<CardsContent> cards, String dominantMood, Map<String, Double> moodVector) {
        // 这里需要你定义匹配规则，例如：
        // - 悲伤 -> 鼓舞人心的卡片
        // - 焦虑 -> 平静的卡片
        // - 平静 -> 启发思考的卡片

        List<CardsContent> matchingCards = new ArrayList<>();

        for (CardsContent card : cards) {
            String tags = card.getTags().replaceAll("[\\[\\]\"]", "");
            if (shouldRecommendForMood(tags, dominantMood, moodVector)) {
                matchingCards.add(card);
            }
        }

        if (!matchingCards.isEmpty()) {
            return matchingCards.get(new Random().nextInt(matchingCards.size()));
        }

        return null;
    }

    // 定义情绪-卡片匹配规则（需要你根据业务需求完善）
    private boolean shouldRecommendForMood(String tags, String dominantMood, Map<String, Double> moodVector) {
        // 示例规则：
        return switch (dominantMood) {
            case "sad" -> tags.contains("hopeful") || tags.contains("encouraging") || tags.contains("inspiring");
            case "anxious" -> tags.contains("calm") || tags.contains("peaceful") || tags.contains("mindful");
            case "happy" -> tags.contains("inspiring") || tags.contains("wise") || tags.contains("progress");
            default -> tags.contains("calm") || tags.contains("peaceful");
        };
    }
}