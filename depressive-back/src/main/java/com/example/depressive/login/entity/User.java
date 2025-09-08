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

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
public class User implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String username;

    @Column(nullable = false, unique = true)
    private String phone;

    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    // 实现UserDetails接口方法
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Collections.emptyList(); // 返回用户权限列表
    }

    @Override
    public String getPassword() {
        return passwordHash; // 返回加密后的密码
    }

    @Override
    public boolean isAccountNonExpired() {
        return true; // 账户是否未过期
    }

    @Override
    public boolean isAccountNonLocked() {
        return true; // 账户是否未锁定
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true; // 凭证是否未过期
    }

    @Override
    public boolean isEnabled() {
        return true; // 账户是否启用
    }
}