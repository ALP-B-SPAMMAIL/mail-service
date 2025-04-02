package com.example.mail.event;

import com.example.mail.eventDto.MailSummarizedEventDto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class MailSummarizedEvent extends AbstractEvent {
    private MailSummarizedEventDto payload;

    public MailSummarizedEvent(MailSummarizedEventDto payload) {
        super(payload);
        this.payload = payload;
    }
}