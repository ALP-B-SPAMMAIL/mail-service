package com.example.mail.event;

import com.example.mail.eventDto.MailInboundedEventDto;

public class MailInboundedEvent extends AbstractEvent {
    public MailInboundedEvent(MailInboundedEventDto mailSentEventDto) {
        super(mailSentEventDto);
    }
}
