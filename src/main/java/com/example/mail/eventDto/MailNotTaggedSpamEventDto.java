package com.example.mail.eventDto;

import lombok.Data;

@Data
public class MailNotTaggedSpamEventDto extends AbstractDto {
    private int mailId;
    private boolean isSpam;
    private String mailContent;
}
