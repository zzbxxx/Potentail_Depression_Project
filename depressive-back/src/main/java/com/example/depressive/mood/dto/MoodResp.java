package com.example.depressive.mood.dto;

import lombok.Data;

import java.time.LocalDate;
import java.util.Date;

@Data
public class MoodResp {

    private String text;

    private String date;

    private String primaryMood;

    private String events;

}
