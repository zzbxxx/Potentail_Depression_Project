// LoginResponse.java
package com.example.depressive.login.dto;

import lombok.Data;

@Data
public class LoginResponse {
    private int code;       // 0=成功，1=失败
    private String message;
    private String token;   // 登录成功返回
    private String id;
}