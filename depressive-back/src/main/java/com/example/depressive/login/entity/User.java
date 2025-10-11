package com.example.depressive.login.entity;

import com.example.depressive.notification.entity.Notification;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
public class User implements UserDetails {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private String username;

    @Column(unique = true)
    @JsonIgnore // 防止序列化
    private String phone;

    @Column(name = "password_hash")
    @JsonIgnore // 防止序列化
    private String passwordHash;

    @Column(name = "is_guest", nullable = false)
    private Boolean isGuest = true;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "device_info")
    @JsonIgnore // 防止序列化
    private String deviceInfo;

    @Column(name = "nickname")
    private String nickname;

    @Column(name = "email")
    @JsonIgnore // 防止序列化
    private String email;

    @Column(name = "avatar")
    private String avatar;

    @Column(name = "bio")
    private String bio;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    @JsonIgnore
    private List<Notification> notifications = new ArrayList<>();

    @Column(name = "last_ip_location")
    @JsonIgnore // 防止序列化
    private String lastIpLocation;

    @Override
    @JsonIgnore // 防止序列化
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Collections.emptyList();
    }

    @Override
    @JsonIgnore // 防止序列化
    public String getPassword() {
        return passwordHash != null ? passwordHash : "";
    }

    @Override
    public String getUsername() {
        return username != null ? username : "guest_" + id;
    }

    @Override
    @JsonIgnore // 防止序列化
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    @JsonIgnore // 防止序列化
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    @JsonIgnore // 防止序列化
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    @JsonIgnore // 防止序列化
    public boolean isEnabled() {
        return true;
    }
}