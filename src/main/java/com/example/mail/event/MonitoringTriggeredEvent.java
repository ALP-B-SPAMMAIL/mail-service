package com.example.mail.event;

import com.example.mail.eventDto.MonitoringTriggeredEventDto;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MonitoringTriggeredEvent extends AbstractEvent {
    private MonitoringTriggeredEventDto payload;
    
    // Default constructor for Jackson deserialization
    public MonitoringTriggeredEvent() {
        super();
        this.topic = "mail";
    }

    public MonitoringTriggeredEvent(MonitoringTriggeredEventDto monitoringStartedEventDto) {
        super(monitoringStartedEventDto);
        this.topic = "mail";
        this.payload = monitoringStartedEventDto;
    }
}