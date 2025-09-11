package com.example.depressive.mood.controller;

import com.example.depressive.mood.dto.MoodReq;
import com.example.depressive.mood.service.CardService;
import com.example.depressive.mood.service.MoodService;
import com.fasterxml.jackson.core.JsonProcessingException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

import static com.mysql.cj.conf.PropertyKey.logger;

@RestController
@RequestMapping("/api/mood")
@RequiredArgsConstructor
public class MoodController {
    private final MoodService service;
    @PostMapping("/save")
    public ResponseEntity<Map<String, String>> saveMood(@RequestBody MoodReq req) throws JsonProcessingException {
        System.out.println( req);
        service.saveMood(req);
        return ResponseEntity.ok(Map.of("message", "ok"));
    }
}
