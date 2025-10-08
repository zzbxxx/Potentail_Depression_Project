package com.example.depressive.follow.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class IsFollowingResponse {
    private boolean isFollowing;

    // 臨時添加以測試
    public void setIsFollowing(boolean isFollowing) {
        this.isFollowing = isFollowing;
    }
}