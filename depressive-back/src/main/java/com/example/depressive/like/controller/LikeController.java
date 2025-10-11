package com.example.depressive.like.controller;

import com.example.depressive.like.dto.LikeDTO;
import com.example.depressive.like.dto.LikeResponseDTO;
import com.example.depressive.like.dto.LikeStatusDTO;
import com.example.depressive.like.service.LikeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/likes")
public class LikeController {

    @Autowired
    private LikeService likeService;

    @PostMapping
    public ResponseEntity<LikeResponseDTO> createLike(@RequestBody LikeDTO request) {
        try {
            LikeResponseDTO like = likeService.createLike(
                    request.getUserId(),
                    request.getLikeableId(),
                    request.getLikeableType(),
                    request.getCategory()
            );
            return ResponseEntity.ok(like);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        }
    }

    @PostMapping("/unlike")
    public ResponseEntity<Void> unlike(@RequestBody LikeDTO request) {
        try {
            likeService.unlike(
                    request.getUserId(),
                    request.getLikeableId(),
                    request.getLikeableType()
            );
            return ResponseEntity.ok().build();
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    @GetMapping("/isLiked")
    public ResponseEntity<LikeStatusDTO> isLiked(
            @RequestParam Long userId,
            @RequestParam Long likeableId,
            @RequestParam String likeableType) {
        LikeStatusDTO status = likeService.isLiked(userId, likeableId, likeableType);
        return ResponseEntity.ok(status);
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<String> handleRuntimeException(RuntimeException e) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("{\"error\": \"" + e.getMessage() + "\"}");
    }
}
