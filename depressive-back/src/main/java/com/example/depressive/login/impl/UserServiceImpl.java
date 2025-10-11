package com.example.depressive.login.impl;

import com.example.depressive.login.dto.LoginRequest;
import com.example.depressive.login.dto.LoginResponse;
import com.example.depressive.login.dto.RegisterRequest;
import com.example.depressive.login.entity.User;
import com.example.depressive.login.repository.UserRepository;
import com.example.depressive.login.service.UserService;
import com.example.depressive.login.util.JwtTokenUtil;
import com.example.depressive.util.IpLocationUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import jakarta.servlet.http.HttpServletRequest;
import java.util.Optional;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private IpLocationUtil ipLocationUtil; // 注入 IpLocationUtil

    private final BCryptPasswordEncoder passwordEncoder;
    private final JwtTokenUtil jwtTokenUtil;

    public UserServiceImpl(UserRepository userRepository,
                           BCryptPasswordEncoder passwordEncoder,
                           JwtTokenUtil jwtTokenUtil,
                           IpLocationUtil ipLocationUtil) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenUtil = jwtTokenUtil;
        this.ipLocationUtil = ipLocationUtil;
    }

    @Override
    public void register(RegisterRequest request, HttpServletRequest httpRequest) {
        // 檢查用戶名或手機號是否已存在
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
        // 獲取並儲存 IP 屬地
        String clientIp = ipLocationUtil.getClientIp(httpRequest);
        String location = ipLocationUtil.getIpLocation(clientIp);
        user.setLastIpLocation(location);
        userRepository.save(user);
    }

    @Override
    public LoginResponse login(LoginRequest request, HttpServletRequest httpRequest) {
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
        System.out.println("httpResp:"+httpRequest);

        // 更新 IP 屬地
        String clientIp = ipLocationUtil.getClientIp(httpRequest);
        String location = ipLocationUtil.getIpLocation(clientIp);
        user.setLastIpLocation(location);
        userRepository.save(user);

        // 生成 token
        String token = jwtTokenUtil.generateToken(user);

        response.setCode(200);
        response.setMessage("登录成功");
        response.setToken(token);
        response.setId(user.getId().toString());
        return response;
    }
}