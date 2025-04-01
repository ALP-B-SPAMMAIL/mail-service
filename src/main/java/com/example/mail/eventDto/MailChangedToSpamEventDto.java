package com.example.mail.eventDto;

import java.time.LocalDateTime;

import com.example.mail.model.Mail;

import lombok.Data;

@Data
public class MailChangedToSpamEventDto extends AbstractDto {
    private int mailId;
    private String reason;

    public MailChangedToSpamEventDto(Mail mail, String reason) {  
        this.mailId = mail.getMailId();
        this.reason = reason;
    }
}
