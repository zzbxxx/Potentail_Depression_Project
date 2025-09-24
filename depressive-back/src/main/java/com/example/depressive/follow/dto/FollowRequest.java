package com.example.depressive.follow.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class FollowRequest {
    private Long followedId; // 被关注的用户ID
}
