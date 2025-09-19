package com.example.depressive.personalCenter.controller;

import com.example.depressive.personalCenter.dto.UploadResp;
import com.example.depressive.personalCenter.service.AvatarService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/picture")
@CrossOrigin(origins = "http://localhost:5173")
public class AvatarController {

    @Autowired
    private AvatarService avatarService;

    @GetMapping("/get-presigned-url")
    public ResponseEntity<UploadResp> getPresignedUrl(@RequestParam String fileName) {
        return ResponseEntity.ok(avatarService.getPresignedUrl(fileName));
    }

    @GetMapping("/get-presigned-download-url")
    public ResponseEntity<UploadResp> getPresignedDownloadUrl(@RequestParam String key) {
        return ResponseEntity.ok(avatarService.getPresignedDownloadUrl(key));
    }
}