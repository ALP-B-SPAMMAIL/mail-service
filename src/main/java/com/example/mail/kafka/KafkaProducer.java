package com.example.mail.kafka;

import java.nio.charset.StandardCharsets;

import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.header.internals.RecordHeader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import com.example.mail.event.AbstractEvent;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
@Service
public class KafkaProducer {
    private static ObjectMapper objectMapper = new ObjectMapper();
    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    public void publish(AbstractEvent event) throws JsonProcessingException {
        objectMapper.registerModule(new JavaTimeModule());
        ProducerRecord<String, String> record = new ProducerRecord<>("mail", objectMapper.writeValueAsString(event));
        record.headers().add(new RecordHeader("type", event.getEventType().getBytes(StandardCharsets.UTF_8)));
        kafkaTemplate.send(record);
    }
}
