package com.example.mail.eventDto;


import java.time.LocalDateTime;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MonitoringTriggeredEventDto extends AbstractDto {
    private int userId;
    private String serverAddress;
    private String protocolType;
    private String emailAddress;
    private String emailPassword;
    private LocalDateTime lastReadTime;
    
    // Default constructor for Jackson deserialization
    public MonitoringTriggeredEventDto() {
    }
}
