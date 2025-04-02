package com.example.mail.eventDto;

import java.time.LocalDateTime;

import com.example.mail.model.Mail;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MailChangedToSpamEventDto extends AbstractDto {
    private int mailId;
    private String mailContent;
    private String mailSender;
    private String reason;

    public MailChangedToSpamEventDto(Mail mail, String reason) {  
        this.mailId = mail.getMailId();
        this.mailContent = mail.getMailContent();
        this.mailSender = mail.getMailSender();
        this.reason = reason;
        System.out.println("EVENT reason: " + reason);
    }
}
