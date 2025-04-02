package com.example.mail.eventDto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class MailSummarizedEventDto extends AbstractDto {
    private int mailId;
    private String summary;
}