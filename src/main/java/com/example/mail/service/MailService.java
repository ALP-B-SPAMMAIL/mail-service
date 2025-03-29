package com.example.mail.service;

import jakarta.mail.*;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.example.mail.event.MailInboundedEvent;
import com.example.mail.eventDto.MailInboundedEventDto;

import com.example.mail.kafka.KafkaProducer;
import com.example.mail.model.Mail;
import com.example.mail.repository.MailRepository;

import java.util.Properties;
import java.util.concurrent.CompletableFuture;
import java.util.Date;
import java.util.List;
import java.util.Arrays;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;

@Service
@RequiredArgsConstructor
public class MailService {

    private final MailRepository mailRepository;
    private final KafkaProducer kafkaProducer;

    @Async
    public CompletableFuture<List<String>> checkEmails(int employeeId) {
        Mail mail = mailRepository.findById(employeeId).orElseThrow(() -> new RuntimeException("User not found"));
        System.out.println("Checking emails...");
        try {
            List<String> emails = new ArrayList<>();
            String host = mail.getMailServerAddress();
            String folderName = "INBOX";

            Properties properties = new Properties();
            properties.put("mail.store.protocol", mail.getProtocolType());

            Session emailSession = Session.getDefaultInstance(properties);
            Store store = emailSession.getStore(mail.getProtocolType());
            store.connect(host, mail.getMailAddress(), mail.getAuthority());

            Folder emailFolder = store.getFolder(folderName);
            emailFolder.open(Folder.READ_WRITE);

            int totalMessages = emailFolder.getMessageCount() - 1;
            int amount = 10;
            boolean hasMore = true;
            Date processedLastDate = null;
            for (int i = 0; i < totalMessages / amount + 1 && hasMore; i++) {
                int start = (int)(totalMessages / amount) == i ? 1 : totalMessages - (i + 1) * amount;
                int end = totalMessages - i * amount;

                Message[] messages = emailFolder.getMessages(start, end);
                Arrays.sort(messages, (m1, m2) -> {
                    try {
                        return m2.getReceivedDate().compareTo(m1.getReceivedDate());
                    } catch (MessagingException e) {
                        e.printStackTrace();
                    }
                    return 0;
                });
                for (Message message : messages) {
                    Date internalDate = message.getReceivedDate();
                    
                    // 마지막 처리된 날짜 이후의 메시지만 처리
                    if (mail.getLastMailTime() != null && 
                        (internalDate.before(LDT2Date(mail.getLastMailTime())) || internalDate.equals(LDT2Date(mail.getLastMailTime())))) {
                            hasMore = false;
                            break;
                        }
    
                    // 메시지 처리 로직
                    processMessage(message, emails);
                    if (processedLastDate == null || internalDate.after(processedLastDate)) {
                        processedLastDate = internalDate;
                    }
                }
            }
            if (processedLastDate != null) {
                mail.setLastMailTime(LocalDateTime.ofInstant(processedLastDate.toInstant(), ZoneId.systemDefault()));
                mailRepository.save(mail);
                System.out.println(mail.getLastMailTime());
            }
            emailFolder.close(false);
            store.close();

            return CompletableFuture.completedFuture(emails);
        } catch (Exception e) {
            e.printStackTrace();
            return CompletableFuture.completedFuture(new ArrayList<>());
        }
    }

    @Transactional
    public void publishEvent() {
        try {
            MailInboundedEventDto mailSentEventDto = new MailInboundedEventDto("Mail Sent");
            MailInboundedEvent event = new MailInboundedEvent(mailSentEventDto);
            kafkaProducer.publish(event);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private void processMessage(Message message, List<String> emailList) {
        try {
            String subject = message.getSubject();

            String mail = "처리된 Subject: " + subject + "Received Date: " + message.getReceivedDate();
            emailList.add(mail);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private Date LDT2Date(LocalDateTime ldt) {
        return java.sql.Timestamp.valueOf(ldt);
    }
}

