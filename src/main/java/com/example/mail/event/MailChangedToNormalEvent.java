package com.example.mail.event;

import com.example.mail.eventDto.MailChangedToNormalEventDto;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MailChangedToNormalEvent extends AbstractEvent {
    private MailChangedToNormalEventDto payload;
    
    // Default constructor for Jackson deserialization
    public MailChangedToNormalEvent() {
        super();
        this.topic = "mail";
    }

    public MailChangedToNormalEvent(MailChangedToNormalEventDto mailChangedToNormalEventDto) {
        super(mailChangedToNormalEventDto);
        this.topic = "mail";
        this.payload = mailChangedToNormalEventDto;
    }
}