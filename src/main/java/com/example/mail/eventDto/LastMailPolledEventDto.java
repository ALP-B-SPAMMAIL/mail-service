package com.example.mail.eventDto;

import java.time.LocalDateTime;

import lombok.Data;

@Data
public class LastMailPolledEventDto extends AbstractDto {
    private int userId;
    private LocalDateTime lastMailArrivedAt;

    public LastMailPolledEventDto(int userId, LocalDateTime lastMailArrivedAt) {  
        this.userId = userId;
        this.lastMailArrivedAt = lastMailArrivedAt;
    }
}
    