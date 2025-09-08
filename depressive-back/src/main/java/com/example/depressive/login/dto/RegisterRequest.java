// RegisterRequest.java
package com.example.depressive.login.dto;

import lombok.Data;

@Data
public class RegisterRequest {
    private String username;
    private String phone;
    private String password;
}