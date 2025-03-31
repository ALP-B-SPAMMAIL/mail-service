package com.example.mail.event;

import com.example.mail.eventDto.LastMailPolledEventDto;

public class LastMailPolledEvent extends AbstractEvent {
    public LastMailPolledEvent(LastMailPolledEventDto lastMailPolledEventDto) {
        super(lastMailPolledEventDto);
        this.topic = "mail";
    }
}
