package com.example.depressive.personalCenter.controller;

import com.example.depressive.login.entity.User;
import com.example.depressive.login.repository.UserRepository;
import com.example.depressive.personalCenter.UserMessageRepository.UserMessageRepository;
import com.example.depressive.personalCenter.dto.PersonalResp;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
@RestController
@RequestMapping("/api/userMessage")
public class UserMessageController {

    @Autowired
    private UserRepository userRepository;

    @GetMapping("/personal")
    public ResponseEntity<PersonalResp> getPersonal(@RequestParam Long userId) {
        // 1. 查库
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("用户不存在"));

        // 2. 组装 VO
        PersonalResp resp = new PersonalResp();
        resp.setNickname(user.getNickname());
        resp.setAvatar(user.getAvatar());
        resp.setEmail(user.getEmail());
        resp.setId(userId);
        // 3. 返回
        return ResponseEntity.ok(resp);
    }

    @PutMapping("/profile")
    public ResponseEntity<Map<String, Object>> updateProfile(@RequestParam String userId, @RequestBody User updatedUser) {
        Map<String, Object> response = new HashMap<>();
        try {
            Long id = Long.parseLong(userId);
            User user = userRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("用户不存在"));

            if (updatedUser.getNickname() != null && !updatedUser.getNickname().isEmpty()) {
                user.setNickname(updatedUser.getNickname());
            }
            if (updatedUser.getEmail() != null) {
                user.setEmail(updatedUser.getEmail());
            }
            if (updatedUser.getAvatar() != null) {
                user.setAvatar(updatedUser.getAvatar());
            }

            userRepository.save(user);
            response.put("success", true);
            response.put("message", "用户信息更新成功");
            return ResponseEntity.ok(response);
        } catch (NumberFormatException e) {
            response.put("success", false);
            response.put("message", "用户信息更新失败: 无效的用户ID");
            return ResponseEntity.status(400).body(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "用户信息更新失败: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }
}