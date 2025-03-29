package com.example.mail.policy;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
public class MailSentPolicy {
    @KafkaListener(topics = "mail", groupId = "mail-2")
    public void listen(String message) {
        System.out.println("MailSentPolicy : " + message);
    }
}
    