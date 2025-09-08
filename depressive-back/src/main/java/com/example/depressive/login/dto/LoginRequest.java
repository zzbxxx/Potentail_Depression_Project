// LoginRequest.java
package com.example.depressive.login.dto;

import lombok.Data;

@Data
public class LoginRequest {
    private String username; // 支持用户名或手机号
    private String password;
}
