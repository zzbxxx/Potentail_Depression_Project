package com.example.depressive.mood.controller;

import com.example.depressive.mood.dto.CardResp;
import com.example.depressive.mood.dto.MoodReq;
import com.example.depressive.mood.service.CardService;
import com.fasterxml.jackson.core.JsonProcessingException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/card")
@RequiredArgsConstructor
public class CardController {
    private final CardService service;

    @GetMapping("/today")
    public CardResp today(@RequestParam Long userId) {
        System.out.println("today:"+userId);
        return service.todayCard(userId);
    }

    @GetMapping("/history")
    public List<CardResp> history(@RequestParam Long userId) {
        return service.history(userId);
    }

    @PostMapping("/mood")
    public void mood(@Valid @RequestBody MoodReq req) throws JsonProcessingException {
        service.saveMood(req);
    }
}
