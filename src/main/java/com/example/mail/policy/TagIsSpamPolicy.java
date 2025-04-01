package com.example.mail.policy;

import com.example.mail.event.NotSpamDetectedEvent;
import com.example.mail.event.SpamDetectedEvent;
import com.example.mail.eventDto.NotSpamDetectedEventDto;
import com.example.mail.eventDto.SpamDetectedEventDto;
import com.example.mail.service.MailService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

@Service
public class TagIsSpamPolicy {
    private final ObjectMapper objectMapper = new ObjectMapper();
    @Autowired
    private MailService mailService;

    @KafkaListener(topics = "mail", groupId = "spam-mail-tag-is-spam-policy")
    public void listen(
            @Header(value = "type", required = false) String type,
            @Payload String data
    ) {
        objectMapper.registerModule(new JavaTimeModule());
        if (type != null && (type.equals("NotSpamDetectedEvent") || type.equals("SpamDetectedEvent"))) {
            try {
                int mailId = -1;
                boolean isSpam = false;

                if (type.equals("NotSpamDetectedEvent")) {
                    NotSpamDetectedEvent event = objectMapper.readValue(data, NotSpamDetectedEvent.class);
                    NotSpamDetectedEventDto payload = event.getPayload();
                    mailId = payload.getMailId();
                    isSpam = false;
                } else if (type.equals("SpamDetectedEvent")) {
                    SpamDetectedEvent event = objectMapper.readValue(data, SpamDetectedEvent.class);
                    SpamDetectedEventDto payload = event.getPayload();
                    mailId = payload.getMailId();
                    isSpam = true;
                } 
                
                if (mailId != -1) {
                    mailService.tagIsSpamOrNot(mailId, isSpam);
                }
                else {
                    System.out.println("Warning: Payload is null");
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
    
