package com.example.depressive.favorite.controller;

import com.example.depressive.favorite.dto.*;
import com.example.depressive.favorite.service.FavoriteService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/favorites")
@RequiredArgsConstructor
public class FavoriteController {

    private final FavoriteService favoriteService;

    @PostMapping("/add")
    public ResponseEntity<FavoriteResponse> addFavorite(@RequestBody FavoriteRequest request,
                                                        @RequestParam Long userId) {
        log.info("添加收藏请求 - 用户ID: {}, 对象ID: {}, 类型: {}",
                userId, request.getFavoriteableId(), request.getFavoriteableType());
        FavoriteResponse response = favoriteService.addFavorite(request, userId);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/remove")
    public ResponseEntity<FavoriteResponse> removeFavorite(@RequestBody FavoriteRequest request,
                                                           @RequestParam Long userId) {
        log.info("取消收藏请求 - 用户ID: {}, 对象ID: {}, 类型: {}",
                userId, request.getFavoriteableId(), request.getFavoriteableType());
        FavoriteResponse response = favoriteService.removeFavorite(request, userId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/list")
    public ResponseEntity<List<FavoriteItemDTO>> getFavorites(@RequestParam Long userId,
                                                              @RequestParam(required = false) String type,
                                                              @RequestParam(required = false) String category) {
        log.info("获取收藏列表 - 用户ID: {}, 类型: {}, 分类: {}", userId, type, category);
        List<FavoriteItemDTO> favorites = favoriteService.getUserFavorites(userId, type, category);
        return ResponseEntity.ok(favorites);
    }

    @GetMapping("/stats")
    public ResponseEntity<FavoriteStatsDTO> getFavoriteStats(@RequestParam Long userId) {
        log.info("获取收藏统计 - 用户ID: {}", userId);
        FavoriteStatsDTO stats = favoriteService.getFavoriteStats(userId);
        return ResponseEntity.ok(stats);
    }

    @GetMapping("/check")
    public ResponseEntity<Boolean> checkFavorited(@RequestParam Long userId,
                                                  @RequestParam Long favoriteableId,
                                                  @RequestParam String favoriteableType) {
        System.out.println("uid:"+userId+"fid:"+favoriteableId+ "ftype:"+ favoriteableType);
        log.info("检查收藏状态 - 用户ID: {}, 对象ID: {}, 类型: {}", userId, favoriteableId, favoriteableType);
        Boolean isFavorited = favoriteService.isFavorited(userId, favoriteableId, favoriteableType);
        return ResponseEntity.ok(isFavorited);
    }
}