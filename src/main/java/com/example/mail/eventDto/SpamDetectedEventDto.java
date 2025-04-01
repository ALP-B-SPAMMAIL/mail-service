package com.example.mail.eventDto;


import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SpamDetectedEventDto extends AbstractDto {
    private int mailId;

}
