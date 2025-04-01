package com.example.mail.eventDto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class NotSpamDetectedEventDto extends AbstractDto {
    private int mailId;
}
