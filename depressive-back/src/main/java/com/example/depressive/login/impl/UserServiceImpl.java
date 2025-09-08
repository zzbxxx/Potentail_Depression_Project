package com.example.depressive.login.impl;

import com.example.depressive.login.dto.LoginRequest;
import com.example.depressive.login.dto.LoginResponse;
import com.example.depressive.login.dto.RegisterRequest;
import com.example.depressive.login.entity.User;
import com.example.depressive.login.repository.UserRepository;
import com.example.depressive.login.service.UserService;
import com.example.depressive.login.util.JwtTokenUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserRepository userRepository;
    private BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    private final JwtTokenUtil jwtTokenUtil ;
    // 使用构造器注入
    public UserServiceImpl(UserRepository userRepository,
                           BCryptPasswordEncoder passwordEncoder,
                           JwtTokenUtil jwtTokenUtil) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenUtil = jwtTokenUtil;
    }
    @Override
    public void register(RegisterRequest request) {
        // 检查用户名或手机号是否已存在
        if (userRepository.findByUsername(request.getUsername()).isPresent()) {
            throw new RuntimeException("用户名已注册");
        }
        if (userRepository.findByPhone(request.getPhone()).isPresent()) {
            throw new RuntimeException("手机号已注册");
        }
        User user = new User();
        user.setUsername(request.getUsername());
        user.setPhone(request.getPhone());
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        userRepository.save(user);
    }

    @Override
    public LoginResponse login(LoginRequest request) {
        Optional<User> userOpt = userRepository.findByUsernameOrPhone(request.getUsername(), request.getUsername());
        LoginResponse response = new LoginResponse();

        if (userOpt.isEmpty()) {
            response.setCode(1);
            response.setMessage("用户名或密码错误");
            return response;
        }

        User user = userOpt.get();
        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            response.setCode(1);
            response.setMessage("用户名或密码错误");
            return response;
        }

        // 生成真实token
        String token = jwtTokenUtil.generateToken((UserDetails) user);

        response.setCode(200);
        response.setMessage("登录成功");
        response.setToken(token);
        response.setId(user.getId().toString());
        return response;
    }
}