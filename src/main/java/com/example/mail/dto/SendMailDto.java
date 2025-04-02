package com.example.mail.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class SendMailDto {
    private int fromUserId;
    private String to;
    private String subject;
    private String content;
}