package com.example.mail.event;

import com.example.mail.eventDto.MailChangedToSpamEventDto;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MailChangedToSpamEvent extends AbstractEvent {
    private MailChangedToSpamEventDto payload;
    
    // Default constructor for Jackson deserialization
    public MailChangedToSpamEvent() {
        super();
        this.topic = "mail";
    }

    public MailChangedToSpamEvent(MailChangedToSpamEventDto mailChangedToSpamEventDto) {
        super(mailChangedToSpamEventDto);
        this.topic = "mail";
        this.payload = mailChangedToSpamEventDto;
    }
}