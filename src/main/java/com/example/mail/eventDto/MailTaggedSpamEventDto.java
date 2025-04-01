package com.example.mail.eventDto;

import lombok.Data;

@Data
public class MailTaggedSpamEventDto extends AbstractDto {
    private int mailId;
    private boolean isSpam;
    private String mailContent;
}
