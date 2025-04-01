package com.example.mail.event;

import com.example.mail.eventDto.SpamDetectedEventDto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SpamDetectedEvent extends AbstractEvent {
    private SpamDetectedEventDto payload;

    public SpamDetectedEvent() {
        super();
    }

    public SpamDetectedEvent(SpamDetectedEventDto spamDetectedEventDto) {
        super(spamDetectedEventDto);
        this.payload = spamDetectedEventDto;
    }
}
