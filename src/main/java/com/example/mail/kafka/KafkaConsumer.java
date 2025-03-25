package com.example.mail.kafka;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
public class KafkaConsumer {

    @KafkaListener(topics = "mail", groupId = "mail")
    public void listen(String message) {
        System.out.println("Received message: " + message);
    }
}
