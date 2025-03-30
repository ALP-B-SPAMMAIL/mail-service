package com.example.mail.service;

import jakarta.mail.*;
import jakarta.mail.internet.MimeMultipart;
import lombok.RequiredArgsConstructor;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.example.mail.event.MailInboundedEvent;
import com.example.mail.eventDto.MailInboundedEventDto;

import com.example.mail.kafka.KafkaProducer;
import com.example.mail.model.Mail;
import com.example.mail.model.Monitoring;
import com.example.mail.repository.MailRepository;
import com.example.mail.repository.MonitoringRepository;

import java.util.Properties;

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
    private final MonitoringRepository monitoringRepository;
    private final KafkaProducer kafkaProducer;

    @Scheduled(fixedDelay = 10000)
    public void checkEmails() {
        List<Monitoring> monitoringList = monitoringRepository.findAll();
        try {
            for (Monitoring monitoring : monitoringList) {
                List<Mail> emails = getEmails(monitoring);
                for (Mail email : emails) {
                    mailRepository.save(email);
                    MailInboundedEventDto mailInboundedEventDto = new MailInboundedEventDto(email);
                    MailInboundedEvent event = new MailInboundedEvent(mailInboundedEventDto);
                    kafkaProducer.publish(event);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }
    }


    private void processMessage(Monitoring monitoring, Message message, List<Mail> emailList) {
        try {
            // Parsing Content
            String textContent = "";
            String htmlContent = "";
            
            if (message.getContent() instanceof MimeMultipart) {
                MimeMultipart multipart = (MimeMultipart) message.getContent();
                for (int i = 0; i < multipart.getCount(); i++) {
                    BodyPart bodyPart = multipart.getBodyPart(i);
                    String contentType = bodyPart.getContentType().toLowerCase();
                    if (contentType.contains("text/plain")) {
                        textContent = bodyPart.getContent().toString();
                    } else if (contentType.contains("text/html")) {
                        htmlContent = bodyPart.getContent().toString();
                    }
                }
            } else {
                String contentType = message.getContentType().toLowerCase();
                if (contentType.contains("text/plain")) {
                    textContent = message.getContent().toString();
                } else if (contentType.contains("text/html")) {
                    htmlContent = message.getContent().toString();
                } else {
                    textContent = message.getContent().toString();
                }
            }

            Date receivedDate = message.getReceivedDate();
            if (receivedDate == null) {
                receivedDate = message.getSentDate();
            }

            if (receivedDate != null) {
                Mail mail = Mail.builder()
                    .userId(monitoring.getUserId())
                    .mailContent(textContent)
                    .mailHtmlContent(htmlContent)
                    .mailSender(message.getFrom()[0].toString())
                    .isSpam(false)
                    .whenArrived(receivedDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime())
                    .build();
                emailList.add(mail);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private Date LDT2Date(LocalDateTime ldt) {
        return java.sql.Timestamp.valueOf(ldt);
    }

    private List<Mail> getEmails(Monitoring monitoring) throws NoSuchProviderException, MessagingException {
        List<Mail> emails = new ArrayList<>();
        String host = monitoring.getServerAddress();
        String folderName = "INBOX";

        Properties properties = new Properties();
        properties.put("mail.store.protocol", monitoring.getProtocolType());

        Session emailSession = Session.getDefaultInstance(properties);
        Store store = emailSession.getStore(monitoring.getProtocolType());
        store.connect(host, monitoring.getEmailAddress(), monitoring.getEmailPassword());

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
                if (monitoring.getLastReadTime() != null && 
                    (internalDate.before(LDT2Date(monitoring.getLastReadTime())) || internalDate.equals(LDT2Date(monitoring.getLastReadTime())))) {
                        hasMore = false;
                        break;
                    }

                // 메시지 처리 로직
                processMessage(monitoring, message, emails);
                if (processedLastDate == null || internalDate.after(processedLastDate)) {
                    processedLastDate = internalDate;
                }
            }
        }
        if (processedLastDate != null) {
            monitoring.setLastReadTime(LocalDateTime.ofInstant(processedLastDate.toInstant(), ZoneId.systemDefault()));
            monitoringRepository.save(monitoring);
            System.out.println(monitoring.getLastReadTime());
        }
        emailFolder.close(false);
        store.close();
        return emails;
    }
}

