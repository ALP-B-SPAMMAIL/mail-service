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
    }

    public NotSpamDetectedEvent(NotSpamDetectedEventDto notSpamDetectedEventDto) {
        super(notSpamDetectedEventDto);
        this.payload = notSpamDetectedEventDto;
    }
}
