package com.example.mail.event;

import com.example.mail.eventDto.MailTaggedSpamEventDto;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MailTaggedSpamEvent extends AbstractEvent {
    private MailTaggedSpamEventDto payload;
    
    // Default constructor for Jackson deserialization
    public MailTaggedSpamEvent() {
        super();
        this.topic = "mail";
    }

    public MailTaggedSpamEvent(MailTaggedSpamEventDto mailTaggedSpamEventDto) {
        super(mailTaggedSpamEventDto);
        this.topic = "mail";
        this.payload = mailTaggedSpamEventDto;
    }
}