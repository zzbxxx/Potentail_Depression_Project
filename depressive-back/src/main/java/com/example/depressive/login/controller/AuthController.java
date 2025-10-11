package com.example.depressive.login.controller;

import com.example.depressive.login.dto.*;
import com.example.depressive.login.service.DeviceAuthService;
import com.example.depressive.login.service.UserService;
import com.example.depressive.util.IpLocationUtil;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private UserService userService;

    @Autowired
    private DeviceAuthService authService;

    @Autowired
    private IpLocationUtil ipLocationUtil; // 注入 IpLocationUtil

    @PostMapping("/register")
    public Object register(@RequestBody RegisterRequest request, HttpServletRequest httpRequest) {
        try {
            userService.register(request, httpRequest); // 傳遞 request 以獲取 IP
            return new LoginResponse() {{
                setCode(0);
                setMessage("注册成功");
            }};
        } catch (Exception e) {
            return new LoginResponse() {{
                setCode(1);
                setMessage(e.getMessage());
            }};
        }
    }

    @PostMapping("/login")
    public LoginResponse login(@RequestBody LoginRequest request, HttpServletRequest httpRequest) {
        return userService.login(request, httpRequest);
    }

    @GetMapping("/check")
    public Map<String, Boolean> check(HttpServletRequest request) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        boolean valid = auth != null &&
                auth.isAuthenticated() &&
                !(auth instanceof AnonymousAuthenticationToken);
        return Map.of("valid", valid);
    }
}