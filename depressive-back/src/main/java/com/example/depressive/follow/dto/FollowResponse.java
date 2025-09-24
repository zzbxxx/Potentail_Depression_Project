package com.example.depressive.follow.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class FollowResponse {
    private int code; // 0: 成功, 1: 失败
    private String message;
}
