package com.example.depressive.mood.controller;

import com.example.depressive.mood.dto.CardHistoryDTO;
import com.example.depressive.mood.dto.CardResp;
import com.example.depressive.mood.service.CardService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/card")
@RequiredArgsConstructor
public class CardController {
    private final CardService service;

    @GetMapping("/getCardToday")
    public CardResp today(@RequestParam Long userId, @RequestParam(defaultValue = "false") Boolean guest) {
        System.out.println("today:" + userId);
        return service.todayCard(userId, guest);
    }

    @GetMapping("/cardHistory")
    public List<CardResp> history(@RequestParam Long userId) {
        return service.history(userId);
    }

    @GetMapping("/BriefCardHistory")
    public List<CardHistoryDTO> briefHistory(@RequestParam Long userId) {
        return service.briefHistory(userId);
    }

    @GetMapping("/getCardByCardId")
    public CardResp getCardByCardId(@RequestParam Long userId, @RequestParam Short contentId) {
        return service.getCardByCardId(userId, contentId);
    }
}