package com.example.mail.event;

import com.example.mail.eventDto.NotSpamDetectedEventDto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class NotSpamDetectedEvent extends AbstractEvent {
    private NotSpamDetectedEventDto payload;

    public NotSpamDetectedEvent() {
        super();
        this.topic = "mail";
    }

    public NotSpamDetectedEvent(NotSpamDetectedEventDto notSpamDetectedEventDto) {
        super(notSpamDetectedEventDto);
        this.topic = "mail";
        this.payload = notSpamDetectedEventDto;
    }
}
