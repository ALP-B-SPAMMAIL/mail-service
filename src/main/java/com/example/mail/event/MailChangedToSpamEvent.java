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
    }

    public MailChangedToSpamEvent(MailChangedToSpamEventDto mailChangedToSpamEventDto) {
        super(mailChangedToSpamEventDto);
        this.payload = mailChangedToSpamEventDto;
    }
}