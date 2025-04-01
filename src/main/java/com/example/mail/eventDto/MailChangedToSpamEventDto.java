package com.example.mail.eventDto;

import java.time.LocalDateTime;

import com.example.mail.model.Mail;

import lombok.Data;

@Data
public class MailChangedToSpamEventDto extends AbstractDto {
    private int mailId;

    public MailChangedToSpamEventDto(Mail mail) {  
        this.mailId = mail.getMailId();
    }
}
