package com.example.mail.event;

import com.example.mail.eventDto.MailNotTaggedSpamEventDto;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MailNotTaggedSpamEvent extends AbstractEvent {
    private MailNotTaggedSpamEventDto payload;
    
    // Default constructor for Jackson deserialization
    public MailNotTaggedSpamEvent() {
        super();
    }

    public MailNotTaggedSpamEvent(MailNotTaggedSpamEventDto mailNotTaggedSpamEventDto) {
        super(mailNotTaggedSpamEventDto);
        this.payload = mailNotTaggedSpamEventDto;
    }
}