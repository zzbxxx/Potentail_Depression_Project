package com.example.depressive.login.service;

import com.example.depressive.login.dto.LoginRequest;
import com.example.depressive.login.dto.LoginResponse;
import com.example.depressive.login.dto.RegisterRequest;

public interface UserService {
    void register(RegisterRequest request);
    LoginResponse login(LoginRequest request);
}