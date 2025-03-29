package com.example.mail.eventDto;

import lombok.Data;

@Data
public class MailInboundedEventDto extends AbstractDto {
    private String content;
    public MailInboundedEventDto(String content) {
        this.content = content;
    }
}
