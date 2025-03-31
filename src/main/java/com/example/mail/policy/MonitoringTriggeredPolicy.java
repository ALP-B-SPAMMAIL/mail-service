package com.example.mail.policy;

import com.example.mail.event.MonitoringTriggeredEvent;
import com.example.mail.eventDto.MonitoringTriggeredEventDto;
import com.example.mail.service.MailService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

@Service
public class MonitoringTriggeredPolicy {
    private final ObjectMapper objectMapper = new ObjectMapper();
    @Autowired
    private MailService mailService;

    @KafkaListener(topics = "mail", groupId = "mail-1")
    public void listen(
            @Header(value = "type", required = false) String type,
            @Payload String data
    ) {
        objectMapper.registerModule(new JavaTimeModule());
        if (type != null && type.equals("MonitoringTriggeredEvent")) {
            try {
                System.out.println("MonitoringTriggeredEvent Received");
                MonitoringTriggeredEvent event = objectMapper.readValue(data, MonitoringTriggeredEvent.class);
                MonitoringTriggeredEventDto payload = event.getPayload();
                if (payload != null) {
                    mailService.pullEmails(payload);
                } else {
                    System.out.println("Warning: Payload is null");
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
    
