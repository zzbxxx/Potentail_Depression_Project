package com.example.depressive.personalCenter.dto;

import lombok.Data;

@Data
public class PersonalResp {

    private String nickname;

    private String avatar;

    private String email;

    private long id;

    private String bio;

    private String lastIpLocation;
}
