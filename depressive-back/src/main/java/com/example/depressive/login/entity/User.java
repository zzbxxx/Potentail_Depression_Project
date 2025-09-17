package com.example.depressive.login.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Collections;

// User.java
@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
public class User implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true) // 移除 nullable = false，允许游客用户名为null
    private String username;

    @Column(unique = true)
    private String phone;

    @Column(name = "password_hash")
    private String passwordHash;

    @Column(name = "is_guest", nullable = false)
    private Boolean isGuest = true; // 添加是否为游客字段

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "device_info")
    private String deviceInfo;

    @Column(name = "nickname")
    private String nickname;

    @Column(name = "email") // 新增 email 字段，可为空
    private String email;

    @Column(name = "avatar") // 新增 avatar 字段，可为空
    private String avatar;
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Collections.emptyList();
    }

    @Override
    public String getPassword() {
        return passwordHash != null ? passwordHash : ""; // 处理null情况
    }

    @Override
    public String getUsername() {
        return username != null ? username : "guest_" + id; // 处理null情况
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}