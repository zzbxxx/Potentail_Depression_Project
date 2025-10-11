package com.example.depressive.login.service;

import com.example.depressive.login.dto.LoginRequest;
import com.example.depressive.login.dto.LoginResponse;
import com.example.depressive.login.dto.RegisterRequest;
import jakarta.servlet.http.HttpServletRequest;

public interface UserService {
    void register(RegisterRequest request, HttpServletRequest httpRequest);
    LoginResponse login(LoginRequest request, HttpServletRequest httpRequest);
}