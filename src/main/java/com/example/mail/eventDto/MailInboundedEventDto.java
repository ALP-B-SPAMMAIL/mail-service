package com.example.mail.eventDto;

import com.example.mail.model.Mail;

import lombok.Data;

@Data
public class MailInboundedEventDto extends AbstractDto {
    private int mailId;
    private String mailContent;
    private String mailSender;
    public MailInboundedEventDto(Mail mail) {  
        this.mailId = mail.getMailId();
        this.mailContent = mail.getMailContent();
        this.mailSender = mail.getMailSender();
    }
}
