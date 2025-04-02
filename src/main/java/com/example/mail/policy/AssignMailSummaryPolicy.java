package com.example.mail.policy;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

import com.example.mail.event.MailSummarizedEvent;
import com.example.mail.eventDto.MailSummarizedEventDto;
import com.example.mail.service.MailService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;



@Service
public class AssignMailSummaryPolicy {
    private final ObjectMapper objectMapper = new ObjectMapper();
    @Autowired
    private MailService mailService;

    @KafkaListener(topics = "mail", groupId = "ai-mail-mail-summarized")
    public void listen(
            @Header(value = "type", required = false) String type,
            @Payload String data
    ) {
        objectMapper.registerModule(new JavaTimeModule());
        if (type != null && type.equals("MailSummarizedEvent")) {
            try {
                MailSummarizedEvent event = objectMapper.readValue(data, MailSummarizedEvent.class);
                MailSummarizedEventDto payload = event.getPayload();
                if (payload != null) {
                    mailService.assignMailSummary(payload);
                } else {
                    System.out.println("Warning: Payload is null");
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
