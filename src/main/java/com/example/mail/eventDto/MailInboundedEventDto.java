package com.example.mail.eventDto;

import java.time.LocalDateTime;

import com.example.mail.model.Mail;

import lombok.Data;

@Data
public class MailInboundedEventDto extends AbstractDto {
    private int mailId;
    private String mailContent;
    private String mailSender;
    private LocalDateTime mailArrivalTime;

    public MailInboundedEventDto(Mail mail) {  
        this.mailId = mail.getMailId();
        this.mailContent = mail.getMailContent();
        System.out.println("EVENT mailContent: " + mailContent);
        this.mailSender = mail.getMailSender();
        this.mailArrivalTime = mail.getArrivedAt();
    }
}
